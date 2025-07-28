// RetrofitClient.kt
package com.example.inventoritoko.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.inventoritoko.utils.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import android.util.Log
// Hapus import com.example.inventoritoko.utils.DoubleAdapter
// Hapus import com.example.inventoritoko.utils.BigDecimalAdapter
import com.google.gson.GsonBuilder // Tetap gunakan GsonBuilder

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object RetrofitClient {

    private var INSTANCE: ApiService? = null
    private const val TAG = "RetrofitClient"

    fun getApiService(context: Context): ApiService {
        if (INSTANCE == null) {
            synchronized(this) {
                if (INSTANCE == null) {
                    val logging = HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }

                    val authInterceptor = Interceptor { chain ->
                        val originalRequest = chain.request()
                        val token = runBlocking {
                            context.dataStore.data.first()[stringPreferencesKey(Constants.AUTH_TOKEN)]
                        }
                        Log.d(TAG, "Retrieved token from DataStore: $token")
                        val requestBuilder = originalRequest.newBuilder()
                        token?.let {
                            requestBuilder.header("Authorization", "Bearer $it")
                            Log.d(TAG, "Adding Authorization header: Bearer $it")
                        } ?: run {
                            Log.w(TAG, "No token found in DataStore. Authorization header not added.")
                        }
                        chain.proceed(requestBuilder.build())
                    }

                    val okHttpClient = OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .addInterceptor(authInterceptor)
                        .build()

                    val gson = GsonBuilder()
                        .setLenient()
                        // <--- HAPUS SEMUA registerTypeAdapter UNTUK Double/BigDecimal DI SINI
                        .create()

                    INSTANCE = Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
                        .create(ApiService::class.java)
                }
            }
        }
        return INSTANCE!!
    }
}