package com.denisshulika.road_radar.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.denisshulika.road_radar.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserLocalStorage(private val context: Context) {
    companion object {
        val UID_KEY = stringPreferencesKey("uid")
        val EMAIL_KEY = stringPreferencesKey("email")
        val NAME_KEY = stringPreferencesKey("name")
        val PHONE_KEY = stringPreferencesKey("phoneNumber")
        val AREA_KEY = stringPreferencesKey("area")
        val REGION_KEY = stringPreferencesKey("region")
        val PHOTO_URL_KEY = stringPreferencesKey("photoUrl")
    }

    suspend fun saveUser(user: UserData) {
        context.dataStore.edit { prefs ->
            prefs[UID_KEY] = user.uid
            prefs[EMAIL_KEY] = user.email
            prefs[NAME_KEY] = user.name
            prefs[PHONE_KEY] = user.phoneNumber
            prefs[AREA_KEY] = user.area
            prefs[REGION_KEY] = user.region
            user.photoUrl?.let { prefs[PHOTO_URL_KEY] = it }
        }
    }

    val userFlow: Flow<UserData?> = context.dataStore.data.map { prefs ->
        val uid = prefs[UID_KEY]
        val email = prefs[EMAIL_KEY]
        val name = prefs[NAME_KEY]
        val phoneNumber = prefs[PHONE_KEY]
        val area = prefs[AREA_KEY]
        val region = prefs[REGION_KEY]
        val photoUrl = prefs[PHOTO_URL_KEY]

        if (uid != null && email != null && name != null && phoneNumber != null && area != null && region != null) {
            UserData(uid, email, name, phoneNumber, area, region, photoUrl)
        } else {
            null
        }
    }

    suspend fun getUserEmail() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[EMAIL_KEY]
        }.firstOrNull()
    }

    suspend fun getUserName() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[NAME_KEY]
        }.firstOrNull()
    }

    suspend fun getUserPhoneNuber() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[PHONE_KEY]
        }.firstOrNull()
    }

    suspend fun getUserArea() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[AREA_KEY]
        }.firstOrNull()
    }

    suspend fun getUserRegion() : String? {
        return context.dataStore.data.map { prefs ->
            prefs[REGION_KEY]
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