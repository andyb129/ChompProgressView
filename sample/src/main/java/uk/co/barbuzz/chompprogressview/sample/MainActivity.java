package uk.co.barbuzz.chompprogressview.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import uk.co.barbuzz.chompprogressview.ChompProgressImageView;

public class MainActivity extends AppCompatActivity
        implements ChompNoshTask.OnTaskFinishedListener {

    private static final String TAG = "MainActivity";

    private static final int BITE_SIZE_PIZZA = 440;
    private static final int BITE_SIZE_DONUT = 470;
    private static final int BITE_SIZE_ICE_CREAM = 290;

    private BottomBar mBottomBar;
    private ChompProgressImageView mChompProgressImageView;
    private AsyncTask<Boolean, Integer, Boolean> mChompNoshTask;
    private AlertDialog infoDialog;
    private Drawable mPizzaDrawable, mDonutDrawable, mIceCreamDrawable;
    private int mMenuId = R.id.pizza_item;

    private View.OnClickListener mImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMenuId == R.id.pizza_item) {
                eatNosh(BITE_SIZE_PIZZA, mPizzaDrawable, false);
            } else if (mMenuId == R.id.donut_item) {
                eatNosh(BITE_SIZE_DONUT, mDonutDrawable, false);
            } else if (mMenuId == R.id.ice_cream_item) {
                eatNosh(BITE_SIZE_ICE_CREAM, mIceCreamDrawable, true);
            }
            mChompProgressImageView.setOnClickListener(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_github) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getResources().getString(R.string.github_link)));
            startActivity(i);
            return true;
        } else if (id == R.id.action_info) {
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskFinished() {
        //re-enable image view onclick
        mChompProgressImageView.setOnClickListener(mImageClickListener);
    }

    private void showInfoDialog() {
        if (infoDialog != null && infoDialog.isShowing()) {
            //do nothing if already showing
        } else {
            infoDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.info_details)
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("More info", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(getResources().getString(R.string.github_link))));
                        }
                    })
                    .create();
            infoDialog.show();
        }
    }

    private void initViews(Bundle savedInstanceState) {
        mPizzaDrawable = getResources().getDrawable(R.drawable.pizza);
        mDonutDrawable = getResources().getDrawable(R.drawable.donut);
        mIceCreamDrawable = getResources().getDrawable(R.drawable.icecream);

        mChompProgressImageView = (ChompProgressImageView) findViewById(R.id.content_main_chomp_progress_imageview);
        mChompProgressImageView.setOnClickListener(mImageClickListener);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItemsFromMenu(R.menu.menu_bottom_bar, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                //cancel last cohomp progress task and store menu id for onclick of image
                stopEating();
                mMenuId = menuItemId;
                // reset the image
                if (menuItemId == R.id.pizza_item) {
                    mChompProgressImageView.setImageDrawableChomp(mPizzaDrawable);
                } else if (menuItemId == R.id.donut_item) {
                    mChompProgressImageView.setImageDrawableChomp(mDonutDrawable);
                } else if (menuItemId == R.id.ice_cream_item) {
                    mChompProgressImageView.setImageDrawableChomp(mIceCreamDrawable);
                }
                mChompProgressImageView.setOnClickListener(mImageClickListener);
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

            }
        });
    }

    private void eatNosh(int biteSize, Drawable imageDrawable, boolean isChompFromTop) {
        if (mChompNoshTask != null) {
            mChompNoshTask.cancel(true);
            mChompNoshTask = null;
            mChompProgressImageView.setChompProgress(0);
        }

        mChompProgressImageView.setImageDrawableChomp(imageDrawable);
        mChompProgressImageView.setChompDirection(isChompFromTop ?
                ChompProgressImageView.ChompDirection.TOP :
                ChompProgressImageView.ChompDirection.RANDOM);
        mChompProgressImageView.setBiteRadius(biteSize);
        mChompNoshTask = new ChompNoshTask(mChompProgressImageView, this).execute(true);
    }

    private void stopEating() {
        if (mChompNoshTask != null) {
            mChompNoshTask.cancel(true);
            mChompNoshTask = null;
            mChompProgressImageView.setChompProgress(0);
        }
    }

}
