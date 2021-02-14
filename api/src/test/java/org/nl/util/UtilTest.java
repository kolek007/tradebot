package org.nl.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilTest {


    void findMaxExtremums() {
        double[] testData = new double[]{0, 1, 2, 2, 3, 1, 5, 0};
        final int[] maxExtremums = Util.findMaxExtremums(testData);
        Assertions.assertArrayEquals(new int[]{6, 4}, maxExtremums);
    }
}