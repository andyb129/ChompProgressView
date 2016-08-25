package uk.co.barbuzz.chompprogressview;

import java.util.Random;

/**
 * Created by andyb129 on 06/08/2016.
 */
public class DrawUtils {

    /**
     * calculate the angle made from point p1 between point p2 & p3
     *
     * @param P1X
     * @param P1Y
     * @param P2X
     * @param P2Y
     * @param P3X
     * @param P3Y
     * @return
     */
    public static double calculateAngle(double P1X, double P1Y, double P2X, double P2Y, double P3X, double P3Y){
        double numerator = P2Y*(P1X-P3X) + P1Y*(P3X-P2X) + P3Y*(P2X-P1X);
        double denominator = 1 + (P2Y-P1Y)*(P1Y-P3Y);
        double ratio = numerator/denominator;

        double angleRad = Math.atan(ratio);
        double angleDeg = (angleRad*180)/Math.PI;

        if(angleDeg<0){
            angleDeg = 180+angleDeg;
        }

        return angleDeg;
    }

    /**
     * calculate the smallest angle of a right angled triangle
     *
     * @param oppositeSideLength
     * @param hypotenuseSideLength
     * @return
     */
    public static double calcAngleOfRightAngledTriangle(double oppositeSideLength, double hypotenuseSideLength) {

        double sinOfAngle = oppositeSideLength / hypotenuseSideLength;
        return Math.asin(sinOfAngle) * 180/Math.PI;

    }

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
