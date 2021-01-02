package com.example.fuelcalculator.network

import com.example.fuelcalculator.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.io.IOException


private const val where = " {\"country\": {\"__type\": \"Pointer\", \"className\": \"Country\", \"objectId\": \"vg1c2CxgQ3\"}}"
//private const val BASE_URL = "https://parseapi.back4app.com/"
const val BASE_URL = "https://developerslife.ru"

private const val HEADER_APPLICATION_ID = "X-Parse-Application-Id: qPg5uVkfiC7bTJLvmvSlmHZjFIte9cSvmLlEE2by"
private const val HEADER_CLIENT_KEY = "X-Parse-REST-API-Key: SKpwauX1OzT4xInAwOaP65knVvtBQjpEz784Ajbj"

//private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//
//private val retrofit = Retrofit.Builder()
//    .addConverterFactory(MoshiConverterFactory.create(moshi))
//    .addCallAdapterFactory(CoroutineCallAdapterFactory())
//    .baseUrl(BASE_URL)
//    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build())
    .build()

interface CityApiService {
//    @GET("classes/City?limit=51&order=-population&keys=name,location,cityId&where=$where")
//    @Headers(HEADER_APPLICATION_ID, HEADER_CLIENT_KEY)
//    @GET("classes/Continentscountriescities_City?order=-population&keys=name,location,cityId")
    fun getCities(): Deferred<CityResponse>

    @GET("/random")
    fun getGif(@Query("json") json: Boolean? = true): Call<GifDetails>
}

object CityApi {
    val retrofitService: CityApiService by lazy {
        retrofit.create(CityApiService::class.java)
    }
}
