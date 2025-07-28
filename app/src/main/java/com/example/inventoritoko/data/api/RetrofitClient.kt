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

// DataStore untuk menyimpan token
val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object RetrofitClient {

    private var INSTANCE: ApiService? = null

    fun getApiService(context: Context): ApiService {
        if (INSTANCE == null) {
            synchronized(this) {
                if (INSTANCE == null) {
                    // Interceptor untuk logging
                    val logging = HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }

                    // Interceptor untuk menambahkan token ke header
                    val authInterceptor = Interceptor { chain ->
                        val originalRequest = chain.request()
                        val token = runBlocking {
                            context.dataStore.data.first()[stringPreferencesKey(Constants.AUTH_TOKEN)]
                        }
                        val requestBuilder = originalRequest.newBuilder()
                        token?.let {
                            requestBuilder.header("Authorization", "Bearer $it")
                        }
                        chain.proceed(requestBuilder.build())
                    }

                    val okHttpClient = OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .addInterceptor(authInterceptor)
                        .build()

                    INSTANCE = Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(ApiService::class.java)
                }
            }
        }
        return INSTANCE!!
    }
}