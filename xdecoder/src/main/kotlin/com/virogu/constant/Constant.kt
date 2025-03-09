package com.virogu.constant

import java.util.prefs.Preferences

object Constant {
    val PRI_KEY = ""
    val PUB_KEY = ""
    private val preferences = Preferences.userRoot()

    fun getPrivateKey(): String {
        return preferences.get("privateKey", PRI_KEY)
    }

    fun setPrivateKey(key: String) {
        preferences.put("privateKey", key)
    }
}
