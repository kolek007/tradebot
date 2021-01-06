package org.nl.util;

public class Util {

    /**
     * Returns array of size two where first cell contains index of first extremum value and the second cell contains second extremum value
     * @param window - array of prices
     * @return - array with indexes of two extremums
     */
    public static int[] findMaxExtremums(double[] window) {
        final int[] extremum = new int[2];

        for (int i = 0; i < window.length; i++) {
            if(window[i] > extremum[0]){
                extremum[0] = i;
            }
        }

        for (int j = 0; j < window.length; j++) {
            if(window[j] > extremum[1] && j != extremum[0]){
                extremum[1] = j;
            }
        }

        return extremum;
    }
}
