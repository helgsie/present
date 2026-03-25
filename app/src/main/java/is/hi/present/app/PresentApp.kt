package `is`.hi.present.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import `is`.hi.present.core.sync.NetworkMonitor
import `is`.hi.present.core.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PresentApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var networkMonitor: NetworkMonitor
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        syncScheduler.enqueuePeriodicSync()

        applicationScope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online) {
                    syncScheduler.enqueueOneTimeSync()
                }
            }
        }
    }
}