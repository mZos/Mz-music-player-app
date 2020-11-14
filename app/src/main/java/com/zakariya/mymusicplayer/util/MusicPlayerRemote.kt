package com.zakariya.mymusicplayer.util

import android.app.Activity
import android.content.*
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.zakariya.mymusicplayer.model.Song
import com.zakariya.mymusicplayer.services.PlayerService
import java.util.*

object MusicPlayerRemote {

    var playerService: PlayerService? = null
    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()

    fun sendAllSong(songList: MutableList<Song>, position: Int) {
        if (playerService != null) {
            playerService?.getAllSongs(songList, position)
        }
    }

    fun playPause() {
        if (playerService != null)
            playerService?.playPause()
    }

    val songDurationMillis: Int
        get() = if (playerService != null) {
            playerService!!.getSongDurationMillis()
        } else -1

    fun bindToService(context: Context, callback: ServiceConnection): ServiceToken? {

        var realActivity: Activity? = (context as Activity).parent
        if (realActivity == null) {
            realActivity = context
        }

        val contextWrapper = ContextWrapper(realActivity)
        val intent = Intent(contextWrapper, PlayerService::class.java)
        try {
            contextWrapper.startService(intent)
        } catch (ignored: IllegalStateException) {
            ContextCompat.startForegroundService(context, intent)
        }
        val binder = ServiceBinder(callback)

        if (contextWrapper.bindService(
                Intent().setClass(contextWrapper, PlayerService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            mConnectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            playerService = null
        }
    }

    class ServiceBinder internal constructor(private val mCallback: ServiceConnection?) :
        ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PlayerService.LocalBinder
            playerService = binder.getService()
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            playerService = null
        }
    }

    class ServiceToken internal constructor(internal var mWrappedContext: ContextWrapper)
}