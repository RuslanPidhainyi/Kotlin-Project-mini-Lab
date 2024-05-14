package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import pl.wsei.pam.lab01.R
import java.util.Random
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {
    lateinit var size: IntArray;
    lateinit var mBoard: GridLayout;
    lateinit var mBoardModel: MemoryBoardView;

    lateinit var completionPlayer: MediaPlayer
    lateinit var negativePlayer: MediaPlayer
    lateinit var losingPlayer: MediaPlayer

    var isSound: Boolean = true;

    override fun onCreateOptionsMenu(menu: Menu?): Boolean  {
        menuInflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        size = intent.getIntArrayExtra("size") ?: intArrayOf(3, 3);
        mBoard = findViewById(R.id.activity_lab03);
        mBoardModel = MemoryBoardView(mBoard, size[0], size[1]);

        if (savedInstanceState != null) {
            //kod odczytu stanu z savedInstanceState
            //utworznie modelu planszy i przywrócenia stanu zapisanego przed oborotem

            val imagesFromBundle = savedInstanceState.getIntArray("Images");
            val revealedFromBundle = savedInstanceState.getBooleanArray("Revealed");
            val tagsFromBundle = savedInstanceState.getStringArray("Tag");

            // TEST BUNDLE:
//            var test: String = "";
//            tagsFromBundle?.forEach {
//                test = test + " " + it.toString();
//            }
//            Log.i("tags from bundle", test);

            if (imagesFromBundle != null && revealedFromBundle != null && tagsFromBundle != null) {
                mBoardModel.setState(imagesFromBundle, revealedFromBundle, tagsFromBundle);
            }
        }

        mBoardModel.setOnGameChangeListener { e ->
            run {
                when (e.state) {
                    GameStates.Matching -> {
                        e.tiles.forEach { tile ->
                            tile.revealed = true
                        }
                    }

                    GameStates.Match -> {
                        if (isSound) {
                            completionPlayer.start();
                        }

                        e.tiles.forEach { tile ->
                            tile.revealed = true;

                            animatePairedButton(tile.button) {
                                tile.revealed = true
                            };
                        }
                    }

                    GameStates.NoMatch -> {
                        if (isSound) {
                            negativePlayer.start();
                        }

                        e.tiles.forEach { tile ->
                            tile.revealed = true

                            animateUnmatchedButton(tile.button) {
                                tile.revealed = false;
                            }
                        }

                        Timer().schedule(2000) {
                            e.tiles.forEach { tile ->

                                tile.revealed = false
                            }

                            runOnUiThread {
                                e.tiles.forEach { tile ->
                                    tile.revealed = false
                                }
                            }
                        }
                    }

                    GameStates.Finished -> {
                        e.tiles.forEach { tile ->
                            tile.revealed = true

                            animatePairedButton(tile.button) {
                                tile.revealed = true
                            };
                        }
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState);

        outState.putIntArray("Images", mBoardModel.getState().map { it.id }.toIntArray());
        outState.putBooleanArray(
            "Revealed",
            mBoardModel.getState().map { it.revealed }.toBooleanArray()
        );
        outState.putStringArray("Tag", mBoardModel.getState().map { it.tag }.toTypedArray());
    }


    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)
        set.startDelay = 500
        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)
        set.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run();
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    private fun animateUnmatchedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 10f, -10f, 5f, -5f, 0f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.5f, 1f)

        set.startDelay = 500
        set.duration = 1500
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)
        set.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                action.run();
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    override protected fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
        losingPlayer = MediaPlayer.create(applicationContext, R.raw.losing)
    }


    override protected fun onPause() {
        super.onPause();
        completionPlayer.release()
        negativePlayer.release()
        losingPlayer.release()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()){
            R.id.board_activity_sound -> {
                if (isSound) {
                    Toast.makeText(this, "Sound turn off", Toast.LENGTH_SHORT).show();
                    item.setIcon(R.drawable.baseline_volume_off_24)
                    isSound = false;
                } else {
                    Toast.makeText(this, "Sound turn on", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_up_24)
                    isSound = true
                }
            }
        }
        return false
    }
}