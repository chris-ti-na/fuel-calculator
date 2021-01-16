package com.example.fuelcalculator.network.distance

import androidx.lifecycle.LiveData
import com.example.fuelcalculator.network.city.CityProperty
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*


//The expected order for all coordinates arrays is [lon, lat]
//All default timings are in seconds
//All default distances are in meters

//37.61556,55.75222 - msk
//30.31413,59.93863 - s.p.
//50.15,53.20007 - smr
//[[30.31413,59.93863],[37.61556,55.75222]]

private const val BASE_URL = "https://api.openrouteservice.org/"

private const val HEADER_AUTHORIZATION =
    "Authorization: 5b3ce3597851110001cf62480c9edafa6baa41289fffbc4d6ad731c4"
private const val HEADER_CONTENT_TYPE = "Content-Type: application/json; charset=utf-8"

private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addConverterFactory(ScalarsConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()


object DistanceApi {
    val retrofitService: DistanceApiService by lazy {
        retrofit.create(DistanceApiService::class.java)
    }
}

interface DistanceApiService {
    @Headers(HEADER_AUTHORIZATION, HEADER_CONTENT_TYPE)
    @POST("v2/matrix/driving-car")
    fun getDistance(@Body params: RequestBody?): Deferred<DistanceResponse>
}

fun createJsonRequestBody(
    departure: LiveData<CityProperty>,
    destination: LiveData<CityProperty>
): RequestBody? {
    //{"locations":[[50.15,53.20007],[37.61556,55.75222]],"destinations":[1],"id":"matrix","metrics":["distance"],"sources":[0],"units":"km"}
    if (departure.value != null && destination.value != null) {
        val body = mapOf(
            "id" to "matrix",
            "metrics" to arrayListOf("distance"),
            "sources" to arrayListOf(0),
            "destinations" to arrayListOf(1),
            "units" to "km",
            "locations" to arrayListOf<List<Double?>>(
                arrayListOf(
                    departure.value?.location?.longitude,
                    departure.value?.location?.latitude
                ), arrayListOf(
                    destination.value?.location?.longitude,
                    destination.value?.location?.latitude
                )
            )
        )

        return RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            JSONObject(body).toString()
        )
    } else {
        throw IllegalArgumentException("Departure and/or destination cities are null!")
    }
}