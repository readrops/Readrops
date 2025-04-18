package com.readrops.app.more.debug

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.MainActivity
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.more.preferences.components.BasePreference
import com.readrops.app.more.preferences.components.PreferenceHeader
import com.readrops.app.sync.SyncWorker.Companion.ACCOUNT_ID_KEY
import com.readrops.app.sync.SyncWorker.Companion.ITEM_ID_KEY
import com.readrops.app.sync.SyncWorker.Companion.SYNC_RESULT_NOTIFICATION_ID
import com.readrops.app.util.components.AndroidScreen
import com.readrops.db.Database
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class DebugScreen : AndroidScreen(), KoinComponent {

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("MissingPermission")
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Debug") },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                PreferenceHeader(stringResource(R.string.notifications))

                BasePreference(
                    title = "Send notification: Single item from single feed",
                    onClick = {
                        coroutineScope.launch {
                            val database = get<Database>()

                            val item = database.itemDao().selectFirst()
                            val account = database.accountDao().selectCurrentAccount().first()

                            val intent = Intent(context, MainActivity::class.java).apply {
                                putExtra(ACCOUNT_ID_KEY, account!!.id)
                                putExtra(ITEM_ID_KEY, item.id)
                            }

                            val notificationBuilder = Builder(context, ReadropsApp.SYNC_CHANNEL_ID)
                                .setContentTitle(item.title)
                                .setContentText("Test notification")
                                .setStyle(
                                    NotificationCompat.BigTextStyle().bigText("Test notification")
                                )
                                .setSmallIcon(R.drawable.ic_notifications)
                                .setContentIntent(
                                    PendingIntent.getActivity(
                                        context,
                                        0,
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )
                                )
                                .setAutoCancel(true)

                            get<NotificationManagerCompat>().notify(
                                SYNC_RESULT_NOTIFICATION_ID,
                                notificationBuilder.build()
                            )
                        }
                    }
                )
            }
        }

    }
}