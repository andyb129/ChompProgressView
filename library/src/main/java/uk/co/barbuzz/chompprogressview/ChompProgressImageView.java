package uk.co.barbuzz.chompprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import uk.co.barbuzz.beerprogressview.R;

/**
 * Custom ImageView to draw some bites over a tasty treat
 *
 * Created by andy barber on 28/07/2016.
 */
public class ChompProgressImageView extends ImageView {

    private static final String TAG = "ChompProgressImageView";

    private static final int BITE_RADIUS_DEFAULT = 400;
    private static final int PROGRESS_MAX_DEFAULT = 100;
    private static final int PROGRESS_DEFAULT = 0;
    private static final int BITE_DIRECTION_DEFAULT = 360; //defaults to all directions selected randomly
    private static final int[] BITE_DIRECTIONS_LIST = new int[]{0, 45, 90, 135, 180, 225, 270, 315};
    private int centerX;
    private int centerY;
    private float circleEdgeX;
    private float circleEdgeY;

    //keep order of the enum below so it matches the array of directions in BITE_DIRECTIONS_LIST
    public enum ChompDirection {
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

    private int imageCircleRadius;
    private int biteDirection;
    private int numBitesPerDirection;
    private int totalNumBites;
    private float biteX, biteY;
    private boolean hasAllBitesTaken;
    private RectF rectF;
    private Drawable drawableChomp;
    private HashMap<Integer, Integer> bitesTakenMap;

    private int chompMax;
    private int chompProgress;
    private int biteRadius;
    private int totalNumberOfBitesTaken;
    private int numBitesForOneDirection;
    private int numBitesForOneDirectionLeft;
    private int chompDirection;

    public ChompProgressImageView(Context context) {
        super(context);
        init();
    }

    public ChompProgressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getAttributes(context, attrs);

        init();
    }

    public ChompProgressImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getAttributes(context, attrs);

        init();
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChompProgressView);

        // a.getInteger can throw RuntimeException or UnsupportedOperationException so surround with try
        try {
            biteRadius = a.getDimensionPixelSize(R.styleable.ChompProgressView_biteRadius, BITE_RADIUS_DEFAULT);
            chompMax = a.getInt(R.styleable.ChompProgressView_chompMax, PROGRESS_MAX_DEFAULT);
            chompProgress = a.getInteger(R.styleable.ChompProgressView_chompProgress, PROGRESS_DEFAULT);
            chompDirection = a.getInteger(R.styleable.ChompProgressView_chompDirection, BITE_DIRECTION_DEFAULT);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        rectF = new RectF();

        //create a map to record bites so we are always moving inwards toward center of circle
        bitesTakenMap = new HashMap<>();
        for (int direction : BITE_DIRECTIONS_LIST) {
            bitesTakenMap.put(direction, 0);
        }

        calcBiteArea();

    }

    private void calcBiteArea() {
        //work out radius of circle the same size as image (which we'll make height/2 at the mo)
        //bite radius added to start slightly further out for first bite
        imageCircleRadius = (Math.max(getDrawable().getIntrinsicHeight()/2,
                getDrawable().getIntrinsicWidth()/2)) + biteRadius;

        //work out number of bites from image size radius of circle bites will be centered around
        //adding 2 extra on to cover the center
        numBitesPerDirection = (imageCircleRadius/biteRadius)+2;
        //also if one direction selected we need bites for circumference
        //(x4 radius to make sure enough bites to eat it all)
        numBitesForOneDirection = (imageCircleRadius*4)/biteRadius;
        numBitesForOneDirectionLeft = numBitesForOneDirection;

        //get total number of bites for progress calc
        totalNumBites = numBitesPerDirection * BITE_DIRECTIONS_LIST.length;
    }

    /**
     * chomp some foodie goodness!
     */
    private void eatImage() {
        //check if bite possible & get bite direction
        if (getBitePosition()) {

            //draw bite and set image drawable
            setBittenImage();
        }
    }

    /**
     * work out place to bite
     */
    private boolean getBitePosition() {

        if (chompDirection==BITE_DIRECTION_DEFAULT) {
            //get random rotation degrees from set list - 0, 45, 90, 135, 180, 225, 270, 315
            //iterate here until a valid bite is selected
            boolean isTakeBite = false;
            while (!hasAllBitesTaken & !isTakeBite) {
                //check if all bites have been taken
                for (Map.Entry<Integer, Integer> entry : bitesTakenMap.entrySet()) {
                    if (entry.getValue() == numBitesPerDirection) {
                        hasAllBitesTaken = true;
                    } else {
                        hasAllBitesTaken = false;
                        break;
                    }
                }
                //get bite direction and record it
                biteDirection = getRandomRotationDegrees();
                if (bitesTakenMap.get(biteDirection) != numBitesPerDirection) {
                    /*int biteCount = bitesTakenMap.get(biteDirection);
                    bitesTakenMap.put(biteDirection, biteCount++);*/
                    isTakeBite = true;
                }
            }
        } else {
            //view set to bite in one direction so just set that
            //and decrement the bite count (reset in the removeBites() method)
            hasAllBitesTaken = (numBitesForOneDirectionLeft == 0);
            if (numBitesForOneDirectionLeft!=0) {
                biteDirection = BITE_DIRECTIONS_LIST[chompDirection];
            }
        }

        // only take bite if all bites have not been taken
        if (!hasAllBitesTaken) {
            int centerX = getDrawable().getIntrinsicWidth() / 2;
            int centerY = getDrawable().getIntrinsicHeight() / 2;

            //get second point as edge of circle
            double angleStartRadian = (biteDirection - 90) * Math.PI / 180;
            float circleEdgeX = (float) (centerX + imageCircleRadius * Math.cos(angleStartRadian));
            float circleEdgeY = (float) (centerY + imageCircleRadius * Math.sin(angleStartRadian));

            this.centerX = centerX;
            this.centerY = centerY;
            this.circleEdgeX = circleEdgeX;
            this.circleEdgeY = circleEdgeY;

            //use points to help get radius line from bite direction & x/y on that line for bite
            double biteLine = DrawUtils.getDistanceBetweenTwoPoints(centerX, centerY, circleEdgeX, circleEdgeY);

            //calc were bite point is for random & one direction
            if (chompDirection==BITE_DIRECTION_DEFAULT) {
                calcBitePoint(centerX, centerY, circleEdgeX, circleEdgeY, biteLine,
                        bitesTakenMap.get(biteDirection) == 0, true);

                //add one to that bite direction if not at max bites
                if (bitesTakenMap.get(biteDirection) < numBitesPerDirection) {
                    bitesTakenMap.put(biteDirection, bitesTakenMap.get(biteDirection) + 1);
                }
            } else {
                calcBitePoint(centerX, centerY, circleEdgeX, circleEdgeY, biteLine,
                        numBitesForOneDirectionLeft == numBitesForOneDirection, false);

                //decrement bite count
                numBitesForOneDirectionLeft -= 1;
            }

            return true;
        }
        return false;
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
    private void calcBitePoint(int centerX, int centerY, float circleEdgeX, float circleEdgeY,
                               double biteLine, boolean isFirstBite, boolean isRandomBite) {
        double bitePercent;
        if (isFirstBite) {
            //start first bite on edge of circle
            biteX = (int) circleEdgeX;
            biteY = (int) circleEdgeY;
        } else {
            //get percentage of biteLine that a bite is
            bitePercent = biteRadius / biteLine;
            if (isRandomBite) {
                bitePercent *= bitesTakenMap.get(biteDirection);
            } else {
                bitePercent *= (numBitesForOneDirection - numBitesForOneDirectionLeft);
            }

            //get position on biteLine with percentage above of bite
            biteX = DrawUtils.getPointBetweenTwoPoints(circleEdgeX, centerX, bitePercent);
            biteY = DrawUtils.getPointBetweenTwoPoints(circleEdgeY, centerY, bitePercent);
        }
    }

    /**
     * get the current image drawable and then draw a bite out of it and re-set it
     */
    private void setBittenImage() {
        Bitmap bitmap = ((BitmapDrawable)getDrawable().mutate()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        setImageDrawable(new BitmapDrawable(getResources(), takeBite(bitmap, biteX, biteY, biteRadius, biteDirection)));
    }

    /**
     * get random bite direction from these;
     * 0 (12 o'clock), 45, 90, 135, 180, 225, 270, 315
     * @return
     */
    private int getRandomRotationDegrees() {
        int randomInRange = DrawUtils.getRandomInRange(0, 8);
        int biteDirection = BITE_DIRECTIONS_LIST[randomInRange];
        return biteDirection;
    }

    /**
     * take a bite out of the bitmap by painting over it with a teeth shaped mark, nom!
     *
     * @param bitmap
     * @param cx
     * @param cy
     * @param radius
     * @param rotationDegrees - 0 degrees will display the bite semi circle
     *                          at 90 degrees which is the default
     * @return new bitten bitmap
     */
    public Bitmap takeBite(Bitmap bitmap, float cx, float cy, float radius, int rotationDegrees) {
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        //large circle - teeth marks only on one half
        RectF bounds = new RectF(cx-radius, cy-radius, cx+radius, cy+radius);
        rectF.set(bounds);
        c.drawArc(rectF, rotationDegrees, 360, true, paint);

        //draw semi circles on large semi circle (nom, nom, teeth)
        int numTeeth = 9;
        int angleSlice = 180 / numTeeth;
        for (int i = 0; i < numTeeth; i++) {
            int angleStart = (angleSlice * i)+rotationDegrees;
            double angleStartRadian = angleStart * Math.PI / 180;
            float xStart = (float) (cx + radius * Math.cos(angleStartRadian));
            float yStart = (float) (cy + radius * Math.sin(angleStartRadian));

            //semi circle x y end
            int angleEnd = (angleSlice * (i+1))+rotationDegrees;
            double angleEndRadian = angleEnd * Math.PI / 180;
            float xEnd = (float) (cx + radius * Math.cos(angleEndRadian));
            float yEnd = (float) (cy + radius * Math.sin(angleEndRadian));

            //calc center of semi circle so we can draw our bound box rectangle
            //which is then used to draw the arc/semi circle
            float circleCenterX = DrawUtils.getPointBetweenTwoPoints(xStart, xEnd, 0.5);
            float circleCenterY = DrawUtils.getPointBetweenTwoPoints(yStart, yEnd, 0.5);

            //now calc circumference of circle from distance between our two points on the circle
            double circumference = DrawUtils.getDistanceBetweenTwoPoints(xStart, yStart, xEnd, yEnd);

            //calculate the degrees need to adjust the small semi circles by so they sit in
            //line with the edge of the bigger semi circle to make the bite! chomp!
            //TODO - make this a bit nicer by combining into formula with numbers given
            //TODO - change this so works with even numTeeth too
            int num = i+1;
            if (num > (numTeeth/2)+0.5) {
                if (num==6) num -= 2;
                if (num==7) num -= 4;
                if (num==8) num -= 6;
                if (num==9) num -= 8;
            }
            double angleOfSmallSemiCircle = ((180 / numTeeth) * num) - 10;

            //draw rectangle and our semi circle
            float radiusSmall = (float) (circumference / 2);
            RectF bounds2 = new RectF(circleCenterX-radiusSmall, circleCenterY- radiusSmall,
                    circleCenterX+ radiusSmall, circleCenterY+ radiusSmall);
            RectF rectTooth = new RectF();
            rectTooth.set(bounds2);

            //angle starts from 90 degrees from the top of the circle when drawing the arc ( 3 o'clock :-) )
            int baseSmallCircleAngleStart = rotationDegrees - 90;
            if (i > numTeeth/2) {
                angleOfSmallSemiCircle =  90 + (90 - angleOfSmallSemiCircle);
            }
            float startAngle = (float) (baseSmallCircleAngleStart + angleOfSmallSemiCircle);

            c.drawArc(rectTooth, (float) startAngle, 180, true, paint);
        }
        return bitmap;
    }

    /** Public methods */

    public void removeBites() {
        //reset bites and drawable
        setImageDrawable(drawableChomp);
        bitesTakenMap = new HashMap<>();
        for (int direction : BITE_DIRECTIONS_LIST) {
            bitesTakenMap.put(direction, 0);
        }
        hasAllBitesTaken = false;
        numBitesForOneDirection = (imageCircleRadius*2)/biteRadius;
    }

    /**
     * set the current chompProgress of the view
     *
     * @param chompProgress
     */
    public void setChompProgress(int chompProgress) {
        int newBites;

        this.chompProgress = chompProgress;
        if (chompProgress > chompMax){
            chompProgress = chompMax;
        }
        if (chompProgress < 0){
            chompProgress = 0;
        }
        float percent = chompProgress * 1.0f / chompMax;
        int numberOfBitesToTake = (int) (percent * totalNumBites);
        if (numberOfBitesToTake > totalNumberOfBitesTaken) {
            newBites = numberOfBitesToTake - totalNumberOfBitesTaken;
            totalNumberOfBitesTaken = numberOfBitesToTake;
        } else {
            newBites = 0;
        }

        //eat! nom! nom!
        for (int i = 1; i <= newBites; i++) {
            eatImage();
        }
    }

    /**
     * get the current chomp progress
     * @return
     */
    public int getChompProgress() {
        return chompProgress;
    }

    /**
     * set the bite radius
     * @return
     */
    public void setBiteRadius(int biteRadius) {
        this.biteRadius = biteRadius;
    }

    /**
     * get the current bite radius
     * @return
     */
    public int getBiteRadius() {
        return biteRadius;
    }

    /**
     * get the maximum chomp progress
     * @return
     */
    public int getChompMax() {
        return chompMax;
    }

    /**
     * set the maximum chomp progress
     * @param chompMax
     */
    public void setChompMax(int chompMax) {
        this.chompMax = chompMax;
    }

    /**
     * get the total number of bites taken
     * @return
     */
    public int getTotalNumberOfBitesTaken() {
        return totalNumberOfBitesTaken;
    }

    /**
     * set the total number of bites taken
     * @param totalNumberOfBitesTaken
     */
    public void setTotalNumberOfBitesTaken(int totalNumberOfBitesTaken) {
        this.totalNumberOfBitesTaken = totalNumberOfBitesTaken;
    }

    /**
     * get the drawable set in the chomp ImageView
     * @return
     */
    public Drawable getDrawableChomp() {
        return drawableChomp;
    }

    /**
     * used to set the chomp ImageView drawable
     * @param drawableChomp
     */
    public void setImageDrawableChomp(Drawable drawableChomp) {
        setImageDrawable(drawableChomp);
        //re-calc image bite area here
        calcBiteArea();
        this.drawableChomp = drawableChomp;
    }

    /**
     * get the chomp direction if set
     * @return
     */
    public int getChompDirection() {
        return chompDirection;
    }

    /**
     * used to set a SINGLE chomp/bite direction INSTEAD of a random one
     * @param chompDirection
     */
    public void setChompDirection(ChompDirection chompDirection) {
        //order of the enum ChompDirection matches the array of directions in BITE_DIRECTIONS_LIST
        //so just set the ordinal (index)
        this.chompDirection = chompDirection==ChompDirection.RANDOM ? BITE_DIRECTION_DEFAULT : chompDirection.ordinal();
    }
}