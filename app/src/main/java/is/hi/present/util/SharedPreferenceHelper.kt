package `is`.hi.present.util

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceHelper @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val MY_PREF_KEY = "MY_PREF"
    }
    private val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
    fun saveStringData(key: String, data: String?) {
        sharedPreferences.edit { putString(key, data) }
    }
    fun getStringData(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
    fun clearPreferences() {
        sharedPreferences.edit { clear() }
    }
}