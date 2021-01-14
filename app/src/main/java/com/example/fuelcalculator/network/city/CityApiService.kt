package com.example.fuelcalculator.network.city

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

//https://parseapi.back4app.com/classes/Continentscountriescities_City?limit=10&order=-population&keys=name,location,cityId&where={"country": {"__type": "Pointer","className": "Continentscountriescities_Country","objectId": "vg1c2CxgQ3"}}

private const val BASE_URL = "https://parseapi.back4app.com/"
val whereCondition = """{"country": {"__type": "Pointer","className": "Continentscountriescities_Country","objectId": "vg1c2CxgQ3"}}"""

private const val HEADER_APPLICATION_ID = "X-Parse-Application-Id: 54euR4X8vK4KjHAE9CRCNcqULGLeQtONviy5Hfar"
private const val HEADER_CLIENT_KEY = "X-Parse-REST-API-Key: ScKhdfV5wm2gqVjehDOGCjpdcgSTeM1QTGWqFvgE"

private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface CityApiService {
    @Headers(HEADER_APPLICATION_ID, HEADER_CLIENT_KEY)
    @GET("classes/Continentscountriescities_City?")
    fun getCities(@Query("limit") limit: String = "50", //todo изменить ограничение на количество городов
                  @Query("order") order: String = "-population",
                  @Query("keys") keys : String = "name,location,cityId",
                  @Query("where") where: String = whereCondition
    ): Deferred<CityResponse>
}

object CityApi {
    val retrofitService: CityApiService by lazy {
        retrofit.create(CityApiService::class.java)
    }
}
