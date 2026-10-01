package br.com.mcoder.primeiroprojeto.data

import android.content.Context
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.model.UserAccount
import org.json.JSONArray
import org.json.JSONObject

class AppStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadUsers(): List<UserAccount> {
        val raw = sharedPreferences.getString(KEY_USERS, EMPTY_ARRAY).orEmpty()
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                add(array.getJSONObject(index).toUserAccount())
            }
        }
    }

    fun saveUsers(users: List<UserAccount>) {
        val array = JSONArray()
        users.forEach { user ->
            array.put(user.toJson())
        }
        sharedPreferences.edit().putString(KEY_USERS, array.toString()).apply()
    }

    fun loadThoughts(): List<ThoughtRecord> {
        val raw = sharedPreferences.getString(KEY_THOUGHTS, EMPTY_ARRAY).orEmpty()
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                add(array.getJSONObject(index).toThoughtRecord())
            }
        }
    }

    fun saveThoughts(thoughts: List<ThoughtRecord>) {
        val array = JSONArray()
        thoughts.forEach { record ->
            array.put(record.toJson())
        }
        sharedPreferences.edit().putString(KEY_THOUGHTS, array.toString()).apply()
    }

    fun loadSessionUserId(): String? = sharedPreferences.getString(KEY_SESSION_USER_ID, null)

    fun saveSessionUserId(userId: String?) {
        sharedPreferences.edit().putString(KEY_SESSION_USER_ID, userId).apply()
    }

    private fun JSONObject.toUserAccount(): UserAccount = UserAccount(
        id = getString("id"),
        name = getString("name"),
        email = getString("email"),
        passwordHash = getString("passwordHash"),
        createdAt = getLong("createdAt")
    )

    private fun UserAccount.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("email", email)
        put("passwordHash", passwordHash)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toThoughtRecord(): ThoughtRecord = ThoughtRecord(
        id = getString("id"),
        userId = getString("userId"),
        situation = getString("situation"),
        automaticThinking = getString("automaticThinking"),
        emotional = getString("emotional"),
        dateTimeMillis = getLong("dateTimeMillis"),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
        emotionIntensity = if (isNull("emotionIntensity")) null else getInt("emotionIntensity"),
        distressingSensation = if (isNull("distressingSensation")) "" else getString("distressingSensation"),
        thoughtBelief = if (isNull("thoughtBelief")) null else getInt("thoughtBelief")
    )

    private fun ThoughtRecord.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("userId", userId)
        put("situation", situation)
        put("distressingSensation", distressingSensation)
        put("automaticThinking", automaticThinking)
        put("thoughtBelief", thoughtBelief ?: JSONObject.NULL)
        put("emotional", emotional)
        put("dateTimeMillis", dateTimeMillis)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("emotionIntensity", emotionIntensity ?: JSONObject.NULL)
    }

    private companion object {
        const val PREFERENCES_NAME = "mind_check_storage"
        const val KEY_USERS = "users"
        const val KEY_THOUGHTS = "thoughts"
        const val KEY_SESSION_USER_ID = "session_user_id"
        const val EMPTY_ARRAY = "[]"
    }
}
