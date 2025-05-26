package com.readrops.app.sync

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.readrops.api.utils.ApiUtils
import com.readrops.app.testutil.ReadropsTestRule
import com.readrops.app.testutil.TestUtils
import com.readrops.app.util.extensions.getSerializable
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 * This test suite runs over [SyncWorker] setup and implementation:
 *  - WorkManagerTestInitHelper is used to test worker setup
 *  - TestListenableWorkerBuilder is used to test worker implementation
 *
 * Notifications are also tested:
 *  - Show notification
 *  - Trigger read and star actions
 *
 * Remaining to test:
 *  - [SyncWorker.startNow] which is currently untestable
 *  - Simultaneous execution between a manual and auto worker
 *  - Notification click (show the right screen)
 */
class SyncWorkerTest : KoinTest {

    private val database: Database by inject()
    private val notificationManager: NotificationManagerCompat by inject()
    private val mockServer = MockWebServer()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val rule = ReadropsTestRule()

    private val localAccount = Account(
        name = "Local account",
        type = AccountType.LOCAL,
        isNotificationsEnabled = true
    )

    private val feverAccount = Account(
        name = "Fever account",
        type = AccountType.FEVER,
    )

    private val localFeed = Feed(
        name = "Hacker news",
        url = mockServer.url("/local").toString(),
        isNotificationEnabled = true
    )

    @Before
    fun before() = runTest {
        //mockServer.start()

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/rss+xml")
                    .setBody(Buffer().readFrom(TestUtils.loadResource("rss_feed_1_item.xml")))
            }

        }

        localAccount.id = database.accountDao().insert(localAccount).toInt()
        feverAccount.id = database.accountDao().insert(feverAccount).toInt()

        localFeed.apply {
            accountId = localAccount.id
            id = database.feedDao().insert(localFeed).toInt()
        }
    }

    @After
    fun after() {
        mockServer.shutdown()
        database.clearAllTables()
        notificationManager.cancelAll()
    }

    @Test
    fun manualWorkerTest() = runTest {
        val worker = TestListenableWorkerBuilder.from<SyncWorker>(context, SyncWorker::class.java)
            .setTags(listOf(SyncWorker.WORK_MANUAL))
            .setInputData(
                workDataOf(
                    SyncWorker.ACCOUNT_ID_KEY to localAccount.id,
                    SyncWorker.FEED_ID_KEY to localFeed.id
                )
            )
            .build()

        val result = worker.doWork()

        assertTrue { result is ListenableWorker.Result.Success }
        assertTrue { result.outputData.getBoolean(SyncWorker.END_SYNC_KEY, false) }

        assertEquals(0, notificationManager.activeNotifications.size)
    }

    @Test
    fun autoWorkerWithNotificationsTest() = runBlocking {
        val worker = TestListenableWorkerBuilder.from<SyncWorker>(context, SyncWorker::class.java)
            .setTags(listOf(SyncWorker.WORK_AUTO))
            .setInputData(
                workDataOf(
                    SyncWorker.ACCOUNT_ID_KEY to localAccount.id,
                    SyncWorker.FEED_ID_KEY to localFeed.id
                )
            )
            .build()

        val result = worker.doWork()

        assertTrue { result is ListenableWorker.Result.Success }
        assertTrue { result.outputData.getBoolean(SyncWorker.END_SYNC_KEY, false) }

        with(notificationManager.activeNotifications.first()) {
            assertEquals(SyncWorker.SYNC_RESULT_NOTIFICATION_ID, id)
            assertEquals(
                "Hacker news",
                this.notification.extras.getString(Notification.EXTRA_TITLE)
            )

            notification.actions.forEach { it.actionIntent.send() }

            // wait for global scope to execute in SyncBroadcastReceiver
            delay(1000L)

            val items = database.itemDao().selectItems(localFeed.id)

            assertTrue { items.first().isRead }
            assertTrue { items.first().isStarred }
        }
    }

    @Test
    fun workerConflictTest() = runTest {
        val workManager = WorkManager.getInstance(context)
        val driver = WorkManagerTestInitHelper.getTestDriver(context)!!

        val request1 = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(SyncWorker.TAG)
            .addTag(SyncWorker.WORK_MANUAL)
            .setInputData(
                workDataOf(
                    SyncWorker.ACCOUNT_ID_KEY to localAccount.id,
                    SyncWorker.FEED_ID_KEY to localFeed.id
                )
            )
            .build()

        val request2 = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(SyncWorker.TAG)
            .addTag(SyncWorker.WORK_MANUAL)
            .setInputData(
                workDataOf(
                    SyncWorker.ACCOUNT_ID_KEY to localAccount.id,
                    SyncWorker.FEED_ID_KEY to localFeed.id
                )
            )
            .build()

        workManager.enqueue(request1)
        workManager.enqueue(request2)

        driver.setAllConstraintsMet(request1.id)
        driver.setAllConstraintsMet(request2.id)

        val workInfos = listOf(
            workManager.getWorkInfoById(request1.id).get(),
            workManager.getWorkInfoById(request2.id).get()
        )

        assertTrue { workInfos.any { it?.state == WorkInfo.State.FAILED } }
        val failedWorkInfo = workInfos.find { it?.state == WorkInfo.State.FAILED }!!
        assertEquals(true, failedWorkInfo.outputData.getBoolean(SyncWorker.SYNC_FAILURE_KEY, false))
        assertNotNull { failedWorkInfo.outputData.getSerializable(SyncWorker.SYNC_FAILURE_EXCEPTION_KEY) }
    }

    @Test
    fun periodicLaunchTest() {
        val workManager = WorkManager.getInstance(context)

        SyncWorker.startPeriodically(context, "1")
        var workInfo = workManager.getWorkInfosByTag(SyncWorker.WORK_AUTO).get()
            .first()
        assertTrue { workInfo.state == WorkInfo.State.ENQUEUED }
        assertEquals(TimeUnit.HOURS.toMillis(1L), workInfo.periodicityInfo?.repeatIntervalMillis)

        SyncWorker.startPeriodically(context, "manual")
        workInfo = workManager.getWorkInfoById(workInfo.id).get()!!
        assertTrue { workInfo.state == WorkInfo.State.CANCELLED }
    }

    @Test
    fun exceptionTest() = runTest {
        val manualWorker =
            TestListenableWorkerBuilder.from<SyncWorker>(context, SyncWorker::class.java)
                .setTags(listOf(SyncWorker.WORK_MANUAL))
                .setInputData(
                    workDataOf(
                        SyncWorker.ACCOUNT_ID_KEY to feverAccount.id,
                    )
                )
                .build()

        val result = manualWorker.doWork()

        assertTrue { result is ListenableWorker.Result.Failure }
        assertTrue { result.outputData.getBoolean(SyncWorker.SYNC_FAILURE_KEY, false) }
        assertNotNull { result.outputData.getSerializable(SyncWorker.SYNC_FAILURE_EXCEPTION_KEY) }

        val autoWorker =
            TestListenableWorkerBuilder.from<SyncWorker>(context, SyncWorker::class.java)
                .setTags(listOf(SyncWorker.WORK_AUTO))
                .setInputData(
                    workDataOf(
                        SyncWorker.ACCOUNT_ID_KEY to feverAccount.id,
                    )
                )
                .build()

        val autoResult = autoWorker.doWork()

        assertTrue { autoResult is ListenableWorker.Result.Failure }
        assertFalse { autoResult.outputData.getBoolean(SyncWorker.SYNC_FAILURE_KEY, false) }
    }

    @Test
    fun localAccountErrorTest() = runTest {
        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
            }
        }

        val worker = TestListenableWorkerBuilder.from<SyncWorker>(context, SyncWorker::class.java)
            .setTags(listOf(SyncWorker.WORK_MANUAL))
            .setInputData(
                workDataOf(
                    SyncWorker.ACCOUNT_ID_KEY to localAccount.id,
                    SyncWorker.FEED_ID_KEY to localFeed.id
                )
            )
            .build()

        val result = worker.doWork()

        assertTrue { result is ListenableWorker.Result.Success }
        assertNotNull { result.outputData.getSerializable(SyncWorker.LOCAL_SYNC_ERRORS_KEY) }
    }
}