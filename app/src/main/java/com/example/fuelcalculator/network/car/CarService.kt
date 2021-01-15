package com.example.fuelcalculator.network.car

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

object CarService {
    private val URL = "https://znanieavto.ru/nuzhno-znat/rasxod-topliva-avtomobilej-tablica.html"

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun loadDoc(): Document = runBlocking {
        val deferredDoc = async {
            withContext(Dispatchers.IO) {
                Jsoup.connect(URL).get()
            }
        }
        return@runBlocking deferredDoc.await()
    }

    private fun parseDoc(doc: Document): Map<String, Double> {
        val carElements = doc.getElementsByClass("spoiler-box__body").toList()
        val format = NumberFormat.getInstance(Locale.FRANCE)

        val cars = hashMapOf<String, Double>()

        for (makeElem in carElements) {
            val modelData = makeElem.select("table tbody tr")
            for (modelElem in modelData) {
                val carData = modelElem.select("td")
                cars[carData[0].text()] = format.parse(carData[1].text()).toDouble()
            }
        }

        return cars
    }

    fun loadCars(): Map<String, Double> {
        return parseDoc(loadDoc())
    }
}