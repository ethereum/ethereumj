package io.enkrypt.kafka.mapping;

import java.util.Set;

public interface ObjectMapping {

  <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value);

  interface Context {

    ObjectMapping mappers();

    Context copy();

    void set(String key, Object value);

    Set<String> keys();

    Object get(String key);

    <T> T get(String key, Class<T> clazz);

  }
}
