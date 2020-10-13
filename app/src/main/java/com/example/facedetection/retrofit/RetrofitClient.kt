package com.example.facedetection.retrofit

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private var retrofitClient: Retrofit? = null
    val client: Retrofit
    get() {
        if(retrofitClient == null)
            retrofitClient = Retrofit.Builder().baseUrl("http://d833d6f301eb.ngrok.io")
                .addConverterFactory(ScalarsConverterFactory.create()).build()
        return retrofitClient!!
    }
}