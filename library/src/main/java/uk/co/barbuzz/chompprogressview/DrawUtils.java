package uk.co.barbuzz.chompprogressview;

import java.util.Random;

/**
 * Created by andyb129 on 06/08/2016.
 */
public class DrawUtils {

    /**
     * get a random number in a range of numbers
     *
     * @param low
     * @param high
     * @return
     */
    public static int getRandomInRange(int low, int high) {
        Random r = new Random();
        return r.nextInt(high - low) + low;
    }

    /**
     * calculate the distance between two points
     *
     * (using pythagoras theorem)
     * var a = x1 - x2
     * var b = y1 - y2
     * var c = Math.sqrt( a*a + b*b );
     *
     * @param xStart
     * @param yStart
     * @param xEnd
     * @param yEnd
     * @return
     */
    public static double getDistanceBetweenTwoPoints(float xStart, float yStart, float xEnd, float yEnd) {
        return Math.sqrt((xStart - xEnd) * (xStart - xEnd) + (yStart - yEnd) * (yStart - yEnd));
    }

    /**
     * get a point between two other points given a decimal percentage
     * i.e. 0.5 will be half way between the two
     *
     * @param xStart
     * @param xEnd
     * @param percentDecimal
     * @return
     */
    public static float getPointBetweenTwoPoints(float xStart, float xEnd, double percentDecimal) {
        return (float) (xStart + percentDecimal * (xEnd - xStart));
    }
}
