package `is`.hi.present.core.sync

import android.content.Context
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
                return Result.success()
            }

            syncManager.syncOwnedData(ownerId)
                .fold(
                    onSuccess = {
                        Result.success()
                    },
                    onFailure = { error ->
                        Result.retry()
                    }
                )
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "owned_data_sync"
        private const val TAG = "SyncWorker"
    }
}