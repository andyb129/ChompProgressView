package uk.co.barbuzz.chompprogressview.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import uk.co.barbuzz.chompprogressview.ChompProgressImageView;

public class MainActivity extends AppCompatActivity implements ChompNoshThread.ChompNoshListener {

    private static final String TAG = "MainActivity";

    private static final int BITE_SIZE_PIZZA = 440;
    private static final int BITE_SIZE_DONUT = 470;
    private static final int BITE_SIZE_ICE_CREAM = 290;

    private BottomNavigationView mBottomBar;
    private ChompProgressImageView mChompProgressImageView;
    private AlertDialog infoDialog;
    private Drawable mPizzaDrawable, mDonutDrawable, mIceCreamDrawable;
    private ChompNoshThread mChompNoshThread;
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

        initViews();
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
    public void onChompNoshProgress(int progress) {
        mChompProgressImageView.setChompProgress(progress);
    }

    @Override
    public void onChompNoshFinished() {
        //reset chomp image view
        mChompProgressImageView.removeBites();
        mChompProgressImageView.setChompProgress(0);
        mChompProgressImageView.setTotalNumberOfBitesTaken(0);
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

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPizzaDrawable = getResources().getDrawable(R.drawable.pizza);
        mDonutDrawable = getResources().getDrawable(R.drawable.donut);
        mIceCreamDrawable = getResources().getDrawable(R.drawable.icecream);

        mChompProgressImageView = findViewById(R.id.content_main_chomp_progress_imageview);
        mChompProgressImageView.setOnClickListener(mImageClickListener);

        mBottomBar = findViewById(R.id.bottom_navigation);
        mBottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //cancel last chomp progress task and store menu id for onclick of image
                stopEating();
                //store menu id so we can use it in image onClick
                mMenuId = item.getItemId();
                switch (mMenuId) {
                    case R.id.pizza_item:
                        mChompProgressImageView.setImageDrawableChomp(mPizzaDrawable);
                        break;
                    case R.id.donut_item:
                        mChompProgressImageView.setImageDrawableChomp(mDonutDrawable);
                        break;
                    case R.id.ice_cream_item:
                        mChompProgressImageView.setImageDrawableChomp(mIceCreamDrawable);
                        break;
                }
                mChompProgressImageView.setOnClickListener(mImageClickListener);
                return true;
            }
        });
    }

    private void eatNosh(int biteSize, Drawable imageDrawable, boolean isChompFromTop) {
        mChompProgressImageView.setImageDrawableChomp(imageDrawable);
        mChompProgressImageView.setChompDirection(isChompFromTop ?
                ChompProgressImageView.ChompDirection.TOP :
                ChompProgressImageView.ChompDirection.RANDOM);
        mChompProgressImageView.setBiteRadius(biteSize);

        mChompNoshThread = new ChompNoshThread(this);
        mChompNoshThread.start();
    }

    private void stopEating() {
        if (mChompNoshThread != null) {
            mChompNoshThread.terminate();
        }
    }
}
