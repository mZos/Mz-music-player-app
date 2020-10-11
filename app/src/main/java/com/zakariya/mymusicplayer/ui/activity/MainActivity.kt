package com.zakariya.mymusicplayer.ui.activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.services.PlayerService
import com.zakariya.mymusicplayer.util.Constants.PREF_NAME
import com.zakariya.mymusicplayer.util.MusicPlayerRemote
import kotlinx.android.synthetic.main.activity_main.*

open class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var playerServiceIntent: Intent

    private val panelState: Int
        get() = bottomSheetBehavior.state

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    dimBackground.visibility = View.GONE
                }
                else -> {
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            setMiniPlayerAlpha(slideOffset)
            setBottomNavigationViewTransition(slideOffset)
            dimBackground.visibility = View.VISIBLE
            dimBackground.alpha = slideOffset
        }
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.LocalBinder
            MusicPlayerRemote.playerService = binder.getService()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            MusicPlayerRemote.playerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        @TargetApi(29)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //make fullscreen, so we can draw behind status bar
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//
//            //make status bar color transparent
//            window.statusBarColor = Color.TRANSPARENT
//            var flags = window.decorView.systemUiVisibility
////            // make dark status bar icons
////            flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
////            window.decorView.systemUiVisibility = flags
//        }

        setContentView(R.layout.activity_main)

        playerServiceIntent = Intent(this, PlayerService::class.java)
        bindService(playerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(playerServiceIntent)

        setUpBottomNavigationNavController()

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        bottomSheetBehavior = BottomSheetBehavior.from(slidingPanel)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        miniPlayerFragment?.view?.setOnClickListener {
            expandPanel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
    }

    override fun onBackPressed() {
        if (!handleBackPress()) super.onBackPressed()
    }

    open fun handleBackPress(): Boolean {
        if (panelState == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel()
            return true
        }
        return false
    }

    fun collapsePanel() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        setMiniPlayerAlpha(0f)
    }

    fun expandPanel() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setMiniPlayerAlpha(1f)
    }

    private fun setMiniPlayerAlpha(slideOffset: Float) {
        val alpha = 1 - slideOffset
        miniPlayerFragment?.view?.alpha = alpha
        miniPlayerFragment?.view?.visibility = if (alpha == 0f) View.GONE else View.VISIBLE
    }

    private fun setBottomNavigationViewTransition(slideOffset: Float) {
        bottomNavigationView.translationY = slideOffset * 500
    }

    private fun setUpBottomNavigationNavController() {
        bottomNavigationView.setupWithNavController(musicNavHostFragment.findNavController())
    }
}