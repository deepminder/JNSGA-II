package org.skaggs.ec.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by skaggsm on 1/22/16.
 */
public class Utils {
    @SafeVarargs
    public static <T> T[] concat(T[]... arrays) {
        int length = Arrays.stream(arrays).mapToInt(value -> value.length).sum();
        T[] newArray = (T[]) Array.newInstance(arrays[0][0].getClass(), length);
        int currentOffset = 0;
        for (T[] currentArray : arrays) {
            System.arraycopy(currentArray, 0, newArray, currentOffset, currentArray.length);
            currentOffset += currentArray.length;
        }
        return newArray;
    }
}
