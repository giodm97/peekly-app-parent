package com.peekly.parent.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "peekly_parent_prefs")

object ParentDataStore {

    private val PARENT_SUB_KEY    = stringPreferencesKey("parent_sub")
    private val FCM_TOKEN_KEY     = stringPreferencesKey("fcm_token")
    private val ONBOARDING_DONE   = booleanPreferencesKey("onboarding_done")

    private fun avatarKey(childId: Long)  = intPreferencesKey("avatar_$childId")
    private fun photoKey(childId: Long)   = stringPreferencesKey("photo_$childId")

    suspend fun getOrCreateParentSub(context: Context): String {
        val stored = context.dataStore.data.first()[PARENT_SUB_KEY]
        if (stored != null) return stored
        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { it[PARENT_SUB_KEY] = generated }
        return generated
    }

    suspend fun storeFcmToken(context: Context, token: String) {
        context.dataStore.edit { it[FCM_TOKEN_KEY] = token }
    }

    suspend fun getFcmToken(context: Context): String? =
        context.dataStore.data.first()[FCM_TOKEN_KEY]

    suspend fun isOnboardingDone(context: Context): Boolean =
        context.dataStore.data.first()[ONBOARDING_DONE] == true

    suspend fun setOnboardingDone(context: Context) {
        context.dataStore.edit { it[ONBOARDING_DONE] = true }
    }

    suspend fun getAvatarIndex(context: Context, childId: Long): Int =
        context.dataStore.data.first()[avatarKey(childId)] ?: 0

    suspend fun setAvatarIndex(context: Context, childId: Long, index: Int) {
        context.dataStore.edit { it[avatarKey(childId)] = index }
    }

    suspend fun getPhotoUri(context: Context, childId: Long): String? =
        context.dataStore.data.first()[photoKey(childId)]

    suspend fun setPhotoUri(context: Context, childId: Long, uri: String?) {
        context.dataStore.edit {
            if (uri != null) it[photoKey(childId)] = uri
            else it.remove(photoKey(childId))
        }
    }
}
