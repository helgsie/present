package `is`.hi.present.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import `is`.hi.present.data.repository.AuthRepository

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val authRepository: AuthRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val ownerId = authRepository.getCurrentUserId()

            if (ownerId == null) {
                Log.w(TAG, "No user logged in, skipping sync")
                return Result.success()
            }

            syncManager.syncOwnedData(ownerId)
                .fold(
                    onSuccess = {
                        Log.d(TAG, "Owned sync finished successfully")
                        Result.success()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Owned sync failed", error)
                        Result.retry()
                    }
                )
        } catch (t: Throwable) {
            Log.e(TAG, "Sync worker crashed", t)
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "owned_data_sync"
        private const val TAG = "SyncWorker"
    }
}