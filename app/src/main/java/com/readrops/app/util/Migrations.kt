package com.readrops.app.util

import android.content.SharedPreferences
import com.readrops.app.BuildConfig
import com.readrops.db.Database
import kotlinx.coroutines.flow.first

object Migrations {

    suspend fun upgrade(
        appPreferences: Preferences,
        encryptedPreferences: SharedPreferences,
        oldPreferences: SharedPreferences,
        database: Database
    ) {
        val lastVersionCode = appPreferences.lastVersionCode.flow
            .first()

        // 2.0-beta02
        if (lastVersionCode < 16) {
            val accounts = database.accountDao().selectAllAccounts().first()

            for (account in accounts) {
                oldPreferences.getString(account.loginKey, null)?.run {
                    encryptedPreferences.edit()
                        .putString(account.loginKey, this)
                        .apply()

                    oldPreferences.edit()
                        .remove(account.loginKey)
                        .apply()
                }

                oldPreferences.getString(account.passwordKey, null)?.run {
                    encryptedPreferences.edit()
                        .putString(account.password, this)
                        .apply()

                    oldPreferences.edit()
                        .remove(account.passwordKey)
                        .apply()
                }
            }
        }

        appPreferences.lastVersionCode.write(BuildConfig.VERSION_CODE)
    }
}