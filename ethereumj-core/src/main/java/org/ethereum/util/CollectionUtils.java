package org.ethereum.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class CollectionUtils {

    public static <T> List<T> selectList(Collection<T> items, Predicate<T> predicate) {
        List<T> selected = new ArrayList<>();
        for(T item : items) {
            if(predicate.evaluate(item)) {
                selected.add(item);
            }
        }
        return selected;
    }

    public interface Predicate<T> {
        boolean evaluate(T item);
    }
}
