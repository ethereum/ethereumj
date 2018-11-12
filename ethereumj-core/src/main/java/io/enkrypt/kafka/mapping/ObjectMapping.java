package io.enkrypt.kafka.mapping;

public interface ObjectMapping {

  <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value);

}
