<div align="center">
  <img src="https://raw.githubusercontent.com/enKryptIO/ethvm/master/.github/assets/logo.png" alt="ethvm-logo">
  <p>:zap::zap::zap: EthVM Project: An open source blockchain explorer for Ethereum network :zap::zap::zap:</p>
  <p>Powered by <a href="https://www.typescriptlang.org/">TypeScript</a> / <a href="https://github.com/socketio/socket.io">Socket.io</a> / <a href="https://github.com/ethereum/ethereumj">EthereumJ</a> / <a href="https://kafka.apache.org/">Kafka</a> / <a href="https://github.com/mongodb/mongo">MongoDB</a> / <a href="https://redis.io/topics/quickstart">Redis</a></p>
</div>

## EthVM: EthereumJ

This is a forked version of [ethereumj](https://github.com/ethereum/ethereumj) client. 

Main differences to the original client are:

- It includes Kafka support that allows to store several important information needed for EthVM.
- It tweaks some parts of the code to fetch more blocks while syncing.
- More soon...

If you're searching for proper instructions on how to compile and use this client, then the better is to go to the [official documentation](https://github.com/enKryptIO/ethereumj).

## License

The go-ethereum library (i.e. all code outside of the `cmd` directory) is licensed under the
[GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html), also
included in our repository in the `COPYING.LESSER` file.

The go-ethereum binaries (i.e. all code inside of the `cmd` directory) is licensed under the
[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html), also included
in our repository in the `COPYING` file.
