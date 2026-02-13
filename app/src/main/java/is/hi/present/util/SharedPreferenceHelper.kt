package `is`.hi.present.util

import android.content.Context
import androidx.core.content.edit

class SharedPreferenceHelper(context: Context) {
    companion object{
        private const val MY_PREF_KEY = "MY_PREF"
    }
    val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
    fun saveStringData(key: String,data: String?){
        sharedPreferences.edit { putString(key, data) }
    }
    fun getStringData(key: String): String?{
        return sharedPreferences.getString(key, null)
    }
    fun clearPreferences(){
        sharedPreferences.edit { clear() }
    }
}