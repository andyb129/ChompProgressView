package uk.co.barbuzz.chompprogressview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import uk.co.barbuzz.beerprogressview.R
import java.util.*

/**
 * Custom ImageView to draw some bites over a tasty treat
 *
 * Created by andy barber on 28/07/2016.
 */
class ChompProgressImageView : ImageView {

    private var centerX: Int = 0
    private var centerY: Int = 0
    private var circleEdgeX: Float = 0.toFloat()
    private var circleEdgeY: Float = 0.toFloat()

    private var imageCircleRadius: Int = 0
    private var biteDirection: Int = 0
    private var numBitesPerDirection: Int = 0
    private var totalNumBites: Int = 0
    private var biteX: Float = 0.toFloat()
    private var biteY: Float = 0.toFloat()
    private var hasAllBitesTaken: Boolean = false
    private var rectF: RectF? = null

    /**
     * get the drawable set in the chomp ImageView
     * @return
     */
    var drawableChomp: Drawable? = null
        private set
    private var bitesTakenMap: HashMap<Int, Int>? = null

    /**
     * get /set the maximum chomp progress
     * @return
     */
    var chompMax: Int = 0
    private var chompProgress: Int = 0

    /**
     * get /set  the current bite radius
     * @return
     */
    var biteRadius: Int = 0

    /**
     * get/set the total number of bites taken
     * @return
     */
    var totalNumberOfBitesTaken: Int = 0
    private var numBitesForOneDirection: Int = 0
    private var numBitesForOneDirectionLeft: Int = 0

    /**
     * get the chomp direction if set
     * @return
     */
    var chompDirection: Int = 0
        private set

    /**
     * work out place to bite
     */
    private val bitePosition: Boolean
        get() {

            if (chompDirection == BITE_DIRECTION_DEFAULT) {
                //get random rotation degrees from set list - 0, 45, 90, 135, 180, 225, 270, 315
                //iterate here until a valid bite is selected
                var isTakeBite = false
                while (!hasAllBitesTaken and !isTakeBite) {
                    for ((_, value) in bitesTakenMap!!) {
                        if (value == numBitesPerDirection) {
                            hasAllBitesTaken = true
                        } else {
                            hasAllBitesTaken = false
                            break
                        }
                    }
                    //get bite direction and record it
                    biteDirection = randomRotationDegrees
                    if (bitesTakenMap!![biteDirection] != numBitesPerDirection) {
                        isTakeBite = true
                    }
                }
            } else {
                //view set to bite in one direction so just set that
                //and decrement the bite count (reset in the removeBites() method)
                hasAllBitesTaken = numBitesForOneDirectionLeft == 0
                if (numBitesForOneDirectionLeft != 0) {
                    biteDirection = BITE_DIRECTIONS_LIST[chompDirection]
                }
            }
            // only take bite if all bites have not been taken
            if (!hasAllBitesTaken) {
                val centerX = drawable.intrinsicWidth / 2
                val centerY = drawable.intrinsicHeight / 2

                //get second point as edge of circle
                val angleStartRadian = (biteDirection - 90) * Math.PI / 180
                val circleEdgeX = (centerX + imageCircleRadius * Math.cos(angleStartRadian)).toFloat()
                val circleEdgeY = (centerY + imageCircleRadius * Math.sin(angleStartRadian)).toFloat()

                this.centerX = centerX
                this.centerY = centerY
                this.circleEdgeX = circleEdgeX
                this.circleEdgeY = circleEdgeY

                //use points to help get radius line from bite direction & x/y on that line for bite
                val biteLine = DrawUtils.getDistanceBetweenTwoPoints(centerX.toFloat(), centerY.toFloat(), circleEdgeX, circleEdgeY)
                if (chompDirection == BITE_DIRECTION_DEFAULT) {
                    calcBitePoint(centerX, centerY, circleEdgeX, circleEdgeY, biteLine,
                            bitesTakenMap!![biteDirection] == 0, true)

                    //add one to that bite direction if not at max bites
                    val bitesTaken = bitesTakenMap!![biteDirection]
                    if (bitesTaken != null) {
                        if (bitesTaken < numBitesPerDirection) {
                            bitesTakenMap!![biteDirection] = bitesTaken + 1
                        }
                    }
                } else {
                    calcBitePoint(centerX, centerY, circleEdgeX, circleEdgeY, biteLine,
                            numBitesForOneDirectionLeft == numBitesForOneDirection, false)
                    //decrement bite count
                    numBitesForOneDirectionLeft -= 1
                }

                return true
            }
            return false
        }

    /**
     * get random bite direction from these;
     * 0 (12 o'clock), 45, 90, 135, 180, 225, 270, 315
     * @return
     */
    private val randomRotationDegrees: Int
        get() {
            val randomInRange = DrawUtils.getRandomInRange(0, 8)
            return BITE_DIRECTIONS_LIST[randomInRange]
        }

    //keep order of the enum below so it matches the array of directions in BITE_DIRECTIONS_LIST
    enum class ChompDirection {
        TOP,
        TOP_RIGHT,
        RIGHT,
        BOTTOM_RIGHT,
        BOTTOM,
        BOTTOM_LEFT,
        LEFT,
        TOP_LEFT,
        RANDOM
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        getAttributes(context, attrs)

        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        getAttributes(context, attrs)

        init()
    }

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ChompProgressView)

        // a.getInteger can throw RuntimeException or UnsupportedOperationException so surround with try
        try {
            biteRadius = a.getDimensionPixelSize(R.styleable.ChompProgressView_biteRadius, BITE_RADIUS_DEFAULT)
            chompMax = a.getInt(R.styleable.ChompProgressView_chompMax, PROGRESS_MAX_DEFAULT)
            chompProgress = a.getInteger(R.styleable.ChompProgressView_chompProgress, PROGRESS_DEFAULT)
            chompDirection = a.getInteger(R.styleable.ChompProgressView_chompDirection, BITE_DIRECTION_DEFAULT)
        } finally {
            a.recycle()
        }
    }

    private fun init() {
        rectF = RectF()

        //create a map to record bites so we are always moving inwards toward center of circle
        bitesTakenMap = HashMap()
        for (direction in BITE_DIRECTIONS_LIST) {
            bitesTakenMap!![direction] = 0
        }

        calcBiteArea()

    }

    private fun calcBiteArea() {
        //work out radius of circle the same size as image (which we'll make height/2 at the mo)
        //bite radius added to start slightly further out for first bite
        imageCircleRadius = Math.max(drawable.intrinsicHeight / 2,
                drawable.intrinsicWidth / 2) + biteRadius

        //work out number of bites from image size radius of circle bites will be centered around
        //adding 2 extra on to cover the center
        numBitesPerDirection = imageCircleRadius / biteRadius + 2
        //also if one direction selected we need bites for circumference
        //(x4 radius to make sure enough bites to eat it all)
        numBitesForOneDirection = imageCircleRadius * 4 / biteRadius
        numBitesForOneDirectionLeft = numBitesForOneDirection

        //get total number of bites for progress calc
        totalNumBites = numBitesPerDirection * BITE_DIRECTIONS_LIST.size
    }

    /**
     * chomp some foodie goodness!
     */
    private fun eatImage() {
        //check if bite possible & get bite direction
        if (bitePosition) {

            //draw bite and set image drawable
            setBittenImage()
        }
    }

    /**
     * method for calculating bite point for random & one direction options
     *
     * @param centerX
     * @param centerY
     * @param circleEdgeX
     * @param circleEdgeY
     * @param biteLine
     * @param isFirstBite
     * @param isRandomBite
     */
    private fun calcBitePoint(centerX: Int, centerY: Int, circleEdgeX: Float, circleEdgeY: Float,
                              biteLine: Double, isFirstBite: Boolean, isRandomBite: Boolean) {
        var bitePercent: Double?
        if (isFirstBite) {
            //start first bite on edge of circle
            biteX = circleEdgeX.toInt().toFloat()
            biteY = circleEdgeY.toInt().toFloat()
        } else {
            //get percentage of biteLine that a bite is
            bitePercent = biteRadius / biteLine
            if (isRandomBite) {
                bitePercent *= bitesTakenMap!![biteDirection]!!.toDouble()
            } else {
                bitePercent *= (numBitesForOneDirection - numBitesForOneDirectionLeft).toDouble()
            }

            //get position on biteLine with percentage above of bite
            biteX = DrawUtils.getPointBetweenTwoPoints(circleEdgeX, centerX.toFloat(), bitePercent)
            biteY = DrawUtils.getPointBetweenTwoPoints(circleEdgeY, centerY.toFloat(), bitePercent)
        }
    }

    /**
     * get the current image drawable and then draw a bite out of it and re-set it
     */
    private fun setBittenImage() {
        val bitmap = (drawable.mutate() as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        setImageDrawable(BitmapDrawable(resources, takeBite(bitmap, biteX, biteY, biteRadius.toFloat(), biteDirection)))
    }

    /**
     * take a bite out of the bitmap by painting over it with a teeth shaped mark, nom!
     *
     * @param bitmap
     * @param cx
     * @param cy
     * @param radius
     * @param rotationDegrees - 0 degrees will display the bite semi circle
     * at 90 degrees which is the default
     * @return new bitten bitmap
     */
    fun takeBite(bitmap: Bitmap, cx: Float, cy: Float, radius: Float, rotationDegrees: Int): Bitmap {
        val c = Canvas(bitmap)
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        //large circle - teeth marks only on one half
        val bounds = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        rectF!!.set(bounds)
        c.drawArc(rectF!!, rotationDegrees.toFloat(), 360f, true, paint)

        //draw semi circles on large semi circle (nom, nom, teeth)
        val numTeeth = 9
        val angleSlice = 180 / numTeeth
        for (i in 0 until numTeeth) {
            val angleStart = angleSlice * i + rotationDegrees
            val angleStartRadian = angleStart * Math.PI / 180
            val xStart = (cx + radius * Math.cos(angleStartRadian)).toFloat()
            val yStart = (cy + radius * Math.sin(angleStartRadian)).toFloat()

            //semi circle x y end
            val angleEnd = angleSlice * (i + 1) + rotationDegrees
            val angleEndRadian = angleEnd * Math.PI / 180
            val xEnd = (cx + radius * Math.cos(angleEndRadian)).toFloat()
            val yEnd = (cy + radius * Math.sin(angleEndRadian)).toFloat()

            //calc center of semi circle so we can draw our bound box rectangle
            //which is then used to draw the arc/semi circle
            val circleCenterX = DrawUtils.getPointBetweenTwoPoints(xStart, xEnd, 0.5)
            val circleCenterY = DrawUtils.getPointBetweenTwoPoints(yStart, yEnd, 0.5)

            //now calc circumference of circle from distance between our two points on the circle
            val circumference = DrawUtils.getDistanceBetweenTwoPoints(xStart, yStart, xEnd, yEnd)

            //calculate the degrees need to adjust the small semi circles by so they sit in
            //line with the edge of the bigger semi circle to make the bite! chomp!
            //TODO - make this a bit nicer by combining into formula with numbers given
            //TODO - change this so works with even numTeeth too
            var num = i + 1
            if (num > numTeeth / 2 + 0.5) {
                if (num == 6) num -= 2
                if (num == 7) num -= 4
                if (num == 8) num -= 6
                if (num == 9) num -= 8
            }
            var angleOfSmallSemiCircle = (180 / numTeeth * num - 10).toDouble()

            //draw rectangle and our semi circle
            val radiusSmall = (circumference / 2).toFloat()
            val bounds2 = RectF(circleCenterX - radiusSmall, circleCenterY - radiusSmall,
                    circleCenterX + radiusSmall, circleCenterY + radiusSmall)
            val rectTooth = RectF()
            rectTooth.set(bounds2)

            //angle starts from 90 degrees from the top of the circle when drawing the arc ( 3 o'clock :-) )
            val baseSmallCircleAngleStart = rotationDegrees - 90
            if (i > numTeeth / 2) {
                angleOfSmallSemiCircle = 90 + (90 - angleOfSmallSemiCircle)
            }
            val startAngle = (baseSmallCircleAngleStart + angleOfSmallSemiCircle).toFloat()

            c.drawArc(rectTooth, startAngle, 180f, true, paint)
        }
        return bitmap
    }

    /** Public methods  */

    fun removeBites() {
        //reset bites and drawable
        setImageDrawable(drawableChomp)
        bitesTakenMap = HashMap()
        for (direction in BITE_DIRECTIONS_LIST) {
            bitesTakenMap!![direction] = 0
        }
        hasAllBitesTaken = false
        numBitesForOneDirection = imageCircleRadius * 2 / biteRadius
    }

    /**
     * set the current chompProgress of the view
     *
     * @param chompProgress
     */
    fun setChompProgressValue(chompProgress: Int) {
        var chompProgress = chompProgress
        val newBites: Int

        this.chompProgress = chompProgress
        if (chompProgress > chompMax) {
            chompProgress = chompMax
        }
        if (chompProgress < 0) {
            chompProgress = 0
        }
        val percent = chompProgress * 1.0f / chompMax
        val numberOfBitesToTake = (percent * totalNumBites).toInt()
        if (numberOfBitesToTake > totalNumberOfBitesTaken) {
            newBites = numberOfBitesToTake - totalNumberOfBitesTaken
            totalNumberOfBitesTaken = numberOfBitesToTake
        } else {
            newBites = 0
        }

        //eat! nom! nom!
        for (i in 1..newBites) {
            eatImage()
        }
    }

    /**
     * used to set the chomp ImageView drawable
     * @param drawableChomp
     */
    fun setImageDrawableChomp(drawableChomp: Drawable) {
        setImageDrawable(drawableChomp)
        //re-calc image bite area here
        calcBiteArea()
        this.drawableChomp = drawableChomp
    }

    /**
     * used to set a SINGLE chomp/bite direction INSTEAD of a random one
     * @param chompDirection
     */
    fun setChompDirection(chompDirection: ChompDirection) {
        //order of the enum ChompDirection matches the array of directions in BITE_DIRECTIONS_LIST
        //so just set the ordinal (index)
        this.chompDirection = if (chompDirection == ChompDirection.RANDOM) BITE_DIRECTION_DEFAULT else chompDirection.ordinal
    }

    companion object {

        private val TAG = "ChompProgressImageView"

        private val BITE_RADIUS_DEFAULT = 400
        private val PROGRESS_MAX_DEFAULT = 100
        private val PROGRESS_DEFAULT = 0
        private val BITE_DIRECTION_DEFAULT = 360 //defaults to all directions selected randomly
        private val BITE_DIRECTIONS_LIST = intArrayOf(0, 45, 90, 135, 180, 225, 270, 315)
    }
}