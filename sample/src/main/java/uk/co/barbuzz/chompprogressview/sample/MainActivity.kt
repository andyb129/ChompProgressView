package uk.co.barbuzz.chompprogressview.sample

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.barbuzz.chompprogressview.ChompProgressImageView

class MainActivity : AppCompatActivity(), ChompNoshThread.ChompNoshListener {

    private lateinit var infoDialog: AlertDialog
    private lateinit var pizzaDrawable: Drawable
    private lateinit var donutDrawable: Drawable
    private lateinit var iceCreamDrawable: Drawable
    private var chompNoshThread: ChompNoshThread? = null
    private var menuId = R.id.pizza_item

    private val imageClickListener = View.OnClickListener {
        when (menuId) {
            R.id.pizza_item -> eatNosh(BITE_SIZE_PIZZA, pizzaDrawable, false)
            R.id.donut_item -> eatNosh(BITE_SIZE_DONUT, donutDrawable, false)
            R.id.ice_cream_item -> eatNosh(BITE_SIZE_ICE_CREAM, iceCreamDrawable, true)
        }
        content_main_chomp_progress_imageview.setOnClickListener(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initViews()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_github) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(resources.getString(R.string.github_link))
            startActivity(i)
            return true
        } else if (id == R.id.action_info) {
            showInfoDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onChompNoshProgress(progress: Int) {
        content_main_chomp_progress_imageview.chompProgress = progress
    }

    override fun onChompNoshFinished() {
        //reset chomp image view
        content_main_chomp_progress_imageview.removeBites()
        content_main_chomp_progress_imageview.chompProgress = 0
        content_main_chomp_progress_imageview.totalNumberOfBitesTaken = 0
        //re-enable image view onclick
        content_main_chomp_progress_imageview.setOnClickListener(imageClickListener)
    }

    private fun showInfoDialog() {
        if (infoDialog.isShowing) {
            //do nothing if already showing
        } else {
            infoDialog = AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.info_details)
                    .setCancelable(true)
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                    .setNegativeButton("More info") { dialog, which ->
                        dialog.dismiss()
                        startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse(resources.getString(R.string.github_link))))
                    }
                    .create()
            infoDialog.show()
        }
    }

    private fun initViews() {
        setSupportActionBar(toolbar)

        pizzaDrawable = resources.getDrawable(R.drawable.pizza)
        donutDrawable = resources.getDrawable(R.drawable.donut)
        iceCreamDrawable = resources.getDrawable(R.drawable.icecream)

        content_main_chomp_progress_imageview.setOnClickListener(imageClickListener)

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            //cancel last chomp progress task and store menu id for onclick of image
            stopEating()
            //store menu id so we can use it in image onClick
            menuId = item.itemId
            when (menuId) {
                R.id.pizza_item -> content_main_chomp_progress_imageview.setImageDrawableChomp(pizzaDrawable)
                R.id.donut_item -> content_main_chomp_progress_imageview.setImageDrawableChomp(donutDrawable)
                R.id.ice_cream_item -> content_main_chomp_progress_imageview.setImageDrawableChomp(iceCreamDrawable)
            }
            content_main_chomp_progress_imageview.setOnClickListener(imageClickListener)
            true
        }
    }

    private fun eatNosh(biteSize: Int, imageDrawable: Drawable?, isChompFromTop: Boolean) {
        content_main_chomp_progress_imageview.setImageDrawableChomp(imageDrawable)
        content_main_chomp_progress_imageview.setChompDirection(if (isChompFromTop)
            ChompProgressImageView.ChompDirection.TOP
        else
            ChompProgressImageView.ChompDirection.RANDOM)
        content_main_chomp_progress_imageview.biteRadius = biteSize

        chompNoshThread = ChompNoshThread(this)
        chompNoshThread?.start()
    }

    private fun stopEating() {
        chompNoshThread?.terminate()
    }

    companion object {
        private const val BITE_SIZE_PIZZA = 440
        private const val BITE_SIZE_DONUT = 470
        private const val BITE_SIZE_ICE_CREAM = 290
    }
}
