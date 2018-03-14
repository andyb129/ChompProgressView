package uk.co.barbuzz.chompprogressview

import java.util.*

/**
 * Created by andyb129 on 06/08/2016.
 */
object DrawUtils {

    /**
     * get a random number in a range of numbers
     *
     * @param low
     * @param high
     * @return
     */
    fun getRandomInRange(low: Int, high: Int): Int {
        val r = Random()
        return r.nextInt(high - low) + low
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
    fun getDistanceBetweenTwoPoints(xStart: Float, yStart: Float, xEnd: Float, yEnd: Float): Double {
        return Math.sqrt(((xStart - xEnd) * (xStart - xEnd) + (yStart - yEnd) * (yStart - yEnd)).toDouble())
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
    fun getPointBetweenTwoPoints(xStart: Float, xEnd: Float, percentDecimal: Double): Float {
        return (xStart + percentDecimal * (xEnd - xStart)).toFloat()
    }
}
