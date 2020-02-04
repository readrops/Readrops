package com.readrops.app.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.readrops.app.repositories.ARepository
import com.readrops.readropsdb.Database
import io.reactivex.disposables.Disposable

class SyncWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    private lateinit var disposable: Disposable

    override fun doWork(): Result {
        val database = Database.getInstance(applicationContext)
        val accounts = database.accountDao().selectAll()
        var result = Result.success()

        accounts.forEach {
            val repository = ARepository.repositoryFactory(it, applicationContext)

            disposable = repository.sync(null)
                    .doOnError { result = Result.failure() }
                    .subscribe()
        }

        return result
    }

    override fun onStopped() {
        super.onStopped()
        disposable.dispose()
    }

    companion object {
        val TAG = SyncWorker::class.java.simpleName
    }
}