package com.dinsdiary

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dinsdiary.data.work.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DinsDiaryApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.dinsdiary.data.prefs.AppLocale.wrap(newBase))
    }

    override fun onCreate() {
        super.onCreate()
        ReminderWorker.createChannel(this)
    }
}
