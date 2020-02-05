package com.readrops.app.utils

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.repositories.ARepository
import com.readrops.readropsdb.Database
import io.reactivex.disposables.Disposable

class SyncWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    private lateinit var disposable: Disposable

    override fun doWork(): Result {
        val database = Database.getInstance(applicationContext)
        val accounts = database.accountDao().selectAll()
        var result = Result.success()

        val notificationBuilder = NotificationCompat.Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.auto_synchro))
                .setProgress(0, 0, true)
                .setSmallIcon(R.drawable.ic_notif)
                .setOnlyAlertOnce(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        accounts.forEach {
            notificationBuilder.setContentText(it.accountName)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

            val repository = ARepository.repositoryFactory(it, applicationContext)

            disposable = repository.sync(null)
                    .doOnError { result = Result.failure() }
                    .subscribe()
        }

        notificationManager.cancel(NOTIFICATION_ID)

        return result
    }

    override fun onStopped() {
        super.onStopped()
        disposable.dispose()
    }

    companion object {
        val TAG = SyncWorker::class.java.simpleName
        private const val NOTIFICATION_ID = 2
    }
}