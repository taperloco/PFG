package com.example.recado.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 *  Communicates with the server
 */
class NetworkService(
    // For testing (bypasses SSL security checks):
    // private val client: OkHttpClient = getUnsafeOkHttpClient(),
    // For deploying:
    //private val client: OkHttpClient = OkHttpClient(),
    private val client: OkHttpClient = getUnsafeOkHttpClient(),
    // For testing (local):
    // "https://192.168.1.132:5000/recado"
    // For deploying:
    // "https://recado-f59c.onrender.com/recado/"
    private val baseUrl: String = "https://192.168.1.132:5000/recado",
) {

    suspend fun login(email: String, password: String): String? = withContext(Dispatchers.IO) {
        val json = Gson().toJson(
            mapOf(
                "email" to email,
                "password" to password
            )
        )
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$baseUrl/login")
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    val json = JSONObject(responseBody)
                    return@withContext json.getString("token")
                }
            } else {
                println("Login failed with code: ${response.code}")
            }
        } catch (e: IOException) {
            println("Network error: ${e.message}")
        }
        return@withContext null
    }

    suspend fun registerUser(name: String, email: String, password: String): String? =
        withContext(Dispatchers.IO) {
            val json = Gson().toJson(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                )
            )
            val body = json.toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("$baseUrl/register")
                .post(body)
                .build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrEmpty()) {
                        val json = JSONObject(responseBody)
                        return@withContext json.getString("token")
                    }
                } else {
                    println("Login failed with code: ${response.code}")
                }
            } catch (e: IOException) {
                println("Network error: ${e.message}")
            }
            return@withContext null
        }

    suspend fun refreshToken(token: String): String? = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder()
            .url("$baseUrl/token")
            .header("Authorization", "Bearer $token")
            .post(RequestBody.create(null, ByteArray(0)))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (continuation.isActive) {
                    if (response.isSuccessful) {
                        val result = response.body?.string() ?: null
                        if (result != null) {
                            continuation.resume(Json.decodeFromString<Token>(result).token)
                        }
                    } else {
                        // Refresh token
                        continuation.resume(null)
                    }
                }
            }
        })
    }

    suspend fun updateData(
        token: String,
        latitude: Double,
        longitude: Double,
        map_radio: Double
    ): UpdateResponse? = suspendCancellableCoroutine { continuation ->
        val json = Gson().toJson(
            mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "map_radio" to map_radio
            )
        )
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$baseUrl/update")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (continuation.isActive) {
                    if (response.isSuccessful) {
                        val result = response.body?.string() ?: "Empty"
                        val lista = Json.decodeFromString<UpdateResponse>(result)
                        continuation.resume(lista)
                    } else {
                        // Null indicates the need to refresh token
                        continuation.resume(null)
                    }
                }
            }
        })
    }

    suspend fun getChat(token: String, send_to: String): String =
        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url("$baseUrl/getchat")
                .header("Authorization", "Bearer $token")
                .addHeader("SendTo", send_to)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        val result = response.body?.string() ?: "Empty"
                        if (result.startsWith("[")) {
                            val chat = Json.decodeFromString<List<Message>>(result)
                            val chatFormatted = chat.joinToString("\n") {
                                "[${it.Date}] ${it.Name}: ${it.Text}"
                            }
                            continuation.resume(chatFormatted)

                        } else {
                            continuation.resume("Chat vacío.")
                        }
                    }
                }
            })
        }

    fun putRecado(token: String, text: String, latitude: Double, longitude: Double) {
        val json = Gson().toJson(
            mapOf(
                "text" to text,
                "latitude" to latitude,
                "longitude" to longitude
            )
        )

        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$baseUrl/recados")
            .header("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("PUT failed: ${response.code}")
                }
            }
        })
    }

    fun putChatMessage(token: String, text: String, send_to: String) {
        val json = Gson().toJson(
            mapOf(
                "text" to text,
                "send_to" to send_to
            )
        )
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$baseUrl/chats")
            .header("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("PUT failed: ${response.code}")
                }
            }
        })
    }
}

// For testing.
fun getUnsafeOkHttpClient(): OkHttpClient {
    val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    val sslSocketFactory = sslContext.socketFactory

    return OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        // Trust all the hosts names
        .hostnameVerifier { _, _ -> true }
        .build()
}