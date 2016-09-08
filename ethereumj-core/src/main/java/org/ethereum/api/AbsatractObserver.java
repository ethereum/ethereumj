package org.ethereum.api;

import org.ethereum.util.Functional;

/**
 * Created by Anton Nashatyrev on 08.09.2016.
 */
public interface AbsatractObserver<T> {

    T getLast();

    void addListener(Functional.Consumer<T> listener);

    void removeListener(Functional.Consumer<T> listener);

    void dispose();

}
