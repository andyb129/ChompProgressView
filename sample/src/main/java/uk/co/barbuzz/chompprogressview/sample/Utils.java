package uk.co.barbuzz.chompprogressview.sample;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by andy.barber on 19/08/2016.
 */
public class Utils {

    private static int sTheme;
    public final static int THEME_DEFAULT = 0;
    public final static int THEME_WHITE = 1;
    public final static int THEME_BLUE = 2;

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            default:
            case THEME_DEFAULT:
                activity.setTheme(R.style.PizzaTheme);
                break;
            case THEME_WHITE:
                activity.setTheme(R.style.DonutTheme);
                break;
            case THEME_BLUE:
                activity.setTheme(R.style.IceCreamTheme);
                break;
        }
    }

}
