package com.peekly.parent.network

import com.peekly.parent.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val instance: PeeklyParentApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PeeklyParentApi::class.java)
    }
}
