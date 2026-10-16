package com.neonforge.launcher.privilege

import com.neonforge.launcher.shizuku.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * HTTP client for the Termux localhost bridge.
 * Bridge runs on 127.0.0.1:38471, whitelist-only commands.
 * Token is read from Termux file (owner must copy-paste into app settings).
 */
class TermuxAdbAdapter(
    private val bridgePort: Int = 38471,
    private val token: String = ""
) {
    private val baseUrl = "http://127.0.0.1:$bridgePort"

    suspend fun isBridgeAlive(): Boolean = withContext(Dispatchers.IO) {
        if (token.isBlank()) return@withContext false
        try {
            val url = URL("$baseUrl/health?token=${URLEncoder.encode(token, "UTF-8")}")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 2000
            conn.readTimeout = 2000
            conn.requestMethod = "GET"
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            response.contains("\"ok\": true")
        } catch (e: Exception) {
            false
        }
    }

    suspend fun runCommand(vararg command: String): CommandResult = withContext(Dispatchers.IO) {
        if (token.isBlank()) {
            return@withContext CommandResult(-10, "", "Bridge token not configured")
        }
        try {
            val params = mutableListOf("token=${URLEncoder.encode(token, "UTF-8")}")
            command.forEach { params.add("argv=${URLEncoder.encode(it, "UTF-8")}") }
            val queryString = params.joinToString("&")
            val url = URL("$baseUrl/exec?$queryString")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.requestMethod = "GET"
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val json = JSONObject(response)
            if (json.has("error")) {
                CommandResult(-1, "", json.getString("error"))
            } else {
                CommandResult(
                    exitCode = json.getInt("exit"),
                    stdout = json.getString("stdout"),
                    stderr = json.getString("stderr")
                )
            }
        } catch (e: Exception) {
            CommandResult(-11, "", "Bridge connection failed: ${e.message}")
        }
    }
}
