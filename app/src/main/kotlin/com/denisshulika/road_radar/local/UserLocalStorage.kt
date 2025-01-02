package com.denisshulika.road_radar.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.denisshulika.road_radar.model.UserData
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserLocalStorage(private val context: Context) {
    companion object {
        val UID_KEY = stringPreferencesKey("uid")
        val EMAIL_KEY = stringPreferencesKey("email")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val NAME_KEY = stringPreferencesKey("name")
        val PHONE_KEY = stringPreferencesKey("phoneNumber")
        val REGION_KEY = stringPreferencesKey("region")
        val DISTRICT_KEY = stringPreferencesKey("district")
        val PHOTO_URL_KEY = stringPreferencesKey("photoUrl")
    }

    suspend fun saveUser(user: UserData) {
        context.dataStore.edit { prefs ->
            prefs[UID_KEY] = user.uid
            prefs[EMAIL_KEY] = user.email
            prefs[PASSWORD_KEY] = user.password
            prefs[NAME_KEY] = user.name
            prefs[PHONE_KEY] = user.phoneNumber
            prefs[REGION_KEY] = user.region
            prefs[DISTRICT_KEY] = user.district
            user.photoUrl?.let { prefs[PHOTO_URL_KEY] = it }
        }
    }

    suspend fun getUserEmail() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[EMAIL_KEY]
        }.firstOrNull()
    }

    suspend fun getUserPassword() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[PASSWORD_KEY]
        }.firstOrNull()
    }

    suspend fun getUserName() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[NAME_KEY]
        }.firstOrNull()
    }

    suspend fun getUserPhoneNumber() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[PHONE_KEY]
        }.firstOrNull()
    }

    suspend fun getUserRegion() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[REGION_KEY]
        }.firstOrNull()
    }

    suspend fun getUserDistrict() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[DISTRICT_KEY]
        }.firstOrNull()
    }

    suspend fun getUserPhotoUrl() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[PHOTO_URL_KEY]
        }.firstOrNull()
    }

    suspend fun clearUserData() {
        context.dataStore.edit { it.clear() }
    }
}