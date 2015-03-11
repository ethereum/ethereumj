# Experimental — New Config Subsystem

Replaces the previous thin-layer-over-two-properties file config system with an elaborate, arguably overdone subsystem.
But the proposed overdone version does have some nice features!

### Backwards compatible

All previous config remains supported. One config key has changed: `peer.discovery` becomes `peer.discovery.enabled`,
but the original key is accepted with a deprecation warning. Keys also now take an `ethereumj` prefix, but that is
added automatically if omitted from configuration in `system.properties` files.

### Plugin architecture

You can rather trivially write plugins to accept configuration from anywhere you please.  See, for example, the [simple plugin](CLIConfigPlugin.java)
that accepts command line overrides from `CLIInterface`, or the 
[plugin factored out](TraditionalPropertiesConfigPlugin.java) of 
the original implementation to read `system.properties` files. Plugin implementations must extend [ConfigPlugin](ConfigPlugin.java) and override one simple method.

Plugins are arranged into a path (configurable via JVM System properties). The first plugin in the path to offer
a non-null value for a key lookup "wins", overriding any config in lower priority plugins.

### Typesafe config / HOCON support

This was my main motivation for mucking about with configuration stuff.

[Typesafe config](https://github.com/typesafehub/config) does not offer the world's most intuitive API, but it does offer 
the JVM's most expressive and powerful config library IMHO. Configuration keys are set hierarchically or as dot separated
paths, which are interpreted identically. Here is a sample "application.conf" file for ethereumj:

```
ethereumj {
    database.reset=true
    dump.style="pretty"
    max.blocks.ask=120
    max.blocks.queued=3000
    max.hashes.ask=1000
    peer {
        channel.read.timeout=30
        connection.timeout=2
        discovery {
            ip.list="poc-7.ethdev.com:30303,185.43.109.23:30303"
            workers=3
        }
        listen.port=10101
    }
    transaction.approve.timeout=15
    vm.structured.dir=vmtrace
    vm.structured.trace=true
}
```

Keys can be written in terms of other keys in ${my.key.name} format (which can be defined anywhere in the config, as long 
as cycles are avoided).

Typesafe config defines nice conventions about configuring defaults in a resource called "reference.conf". Reference.conf
has the same format as application.conf, but it is intended to be provided within a jar file by developers, while
application.conf can be placed on the application CLASSPATH or *at any file or URL* (specified in a System property) to
selectively override defaults. Here is an example [reference.conf](../../../../resources/reference.conf)
file.

"application.conf" can also be specified as "application.properties", for users more comfortable with writing
in properties file format. hierarchical keys are flattened into dot-separated paths.

Typesafe config is especially good at integration. If an application embeds ethereum in a typesafe-config supporting
application server, or uses other typesafe-configgy libraries, you end up with a unified, readable config file like this:

```
ethereumj {
    ...
}
akka {
    ...
}
play {
    ...
}
c3p0 {
    ...
}
```

All of the `reference.conf` files scattered around multiple jars are read and merged into a single config, which
`application.conf` overrides.

System property overrides of any key are also supported.

### Documentation

Despite the increased complexity of the library, definitions of keys, constants, and defaults are centralized in
one, very [readable source code file](KeysDefaults.java).

### Immutability and instantiability

The core config objects, still `SystemProperties`, are now immutable, and `CONFIG` is final. Command-line overrides
are read prior to config initialization so they do not need to mutate `CONFIG`. Immutability is good for lots of reasons,
it offers easy thread safety and eliminates large classes of hard-to-reason-about bugs and attacks.

Although config objects are immutable, they can easily be instantiated, so if the library evolves to support
selection among multiple config objects rather than relying on one static singleton, it will be possible to have multiple
configurations simultaneously active within a single JVM/ClassLoader. Configurations can be derived from other
configurations, including the default configuration, by calling a method `withOverrides(...)`.

Given the current very centralized config architecture, a final and immutable config presents challenges for
testing, since different tests want different configs. Previously, individual tests mutated the central config 
(in particular the database directory) prior to running to get the parameters they expected. That worked fine, as
long as tests are not run in parallel within a single VM. With an immutable central config, that is no longer
possible. However, since all config params can be overridden in System properties, Gradle can be configured to
fork a JVM for each test suite with appropriate parameters configured. (There are some [issues](http://stackoverflow.com/questions/28780841/setting-system-properties-for-gradle-tests)
here, but for ensuring a unique database directory for the various tests it works fine.)

An alternative and perhaps desirable approach would be to make the central CONFIG object replaceable. This could be done simply by making the existing field nonfinal and volatile, or by providing static synchronized accessor and setter methods. It'd probably be a good idea to ensure that access to the main config happens only once, on initialization of an `org.ethereum.facade.Ethereum` rather than continually over its lifecycle, to prevent changes in configuration to alter an the behavior of an already initialized instance and potential violations of consistency assumptions embedded in the config.

### Performance

While this is a much "thicker" configuration layer than before, all known keys are cached in preparsed (often boxed) types,
so performance should be comparable or superior to the Properties-based implementation (perhaps better under Thread 
contention since immutability renders synchronization unnecessary).
