package com.readrops.app.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.readrops.db.Database
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SyncBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val database by inject<Database>()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        NotificationManagerCompat.from(context)
            .cancel(SyncWorker.SYNC_RESULT_NOTIFICATION_ID)

        when (intent.action) {
            ACTION_MARK_READ -> {
                val id = intent.getIntExtra(SyncWorker.ITEM_ID_KEY, -1)
                GlobalScope.launch {
                    database.itemDao().updateReadState(id, true)
                }
            }
            ACTION_SET_FAVORITE -> {
                val id = intent.getIntExtra(SyncWorker.ITEM_ID_KEY, -1)
                GlobalScope.launch {
                    database.itemDao().updateStarState(id, true)
                }
            }
        }
    }

    companion object {
        const val ACTION_MARK_READ = "ACTION_MARK_READ"
        const val ACTION_SET_FAVORITE = "ACTION_SET_FAVORITE"
    }
}