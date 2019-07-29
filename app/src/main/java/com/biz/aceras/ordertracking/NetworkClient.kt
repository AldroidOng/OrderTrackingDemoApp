package com.biz.aceras.ordertracking

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by eesern_ong on 10/4/2019.
 */
class NetworkClient {

    companion object {
        val BASE_URL = "http://172.25.64.104/WebApi1/"

        var retrofit: Retrofit? = null

        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build()


        /*
    This companion object method will return Retrofit client
    anywhere in the appplication
    */
        fun getRetrofitClient(): Retrofit? {

            //If condition to ensure we don't create multiple retrofit instances in a single application
            if (retrofit == null) {
                //Defining the Retrofit using Builder
                retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL) //This is the only mandatory call on Builder object.
                        .addConverterFactory(GsonConverterFactory.create()) // Convertor library used to convert response into POJO
                        .client(okHttpClient)
                        .build()
            }
            return retrofit
        }
    }
}