package com.example.fuelcalculator.network.fuel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object FuelService {
    private val URL = "http://www.benzin-cena.ru/benzin/40-rossija-ceni-v-rubljah"

    var A92: Double = 0.0
        private set
    var A95: Double = 0.0
        private set
    var A98: Double = 0.0
        private set
    var DT: Double = 0.0
        private set

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun loadDoc() : Document = runBlocking {
        val deferredDoc = async {
            withContext(Dispatchers.IO) {
                Jsoup.connect(URL).get()
            }
        }
        val doc = deferredDoc.await()
        return@runBlocking doc
    }


    private fun parseDoc(doc: Document) {
        val table = doc.getElementById("fuel__table")
            .select("tbody").first()
            .select("tr").first()
        A92 = table.select("td")[2].text().toDouble()
        A95 = table.select("td")[3].text().toDouble()
        A98 = table.select("td")[4].text().toDouble()
        DT = table.select("td")[5].text().toDouble()
    }

    fun loadPrices() {
        parseDoc(loadDoc())
    }
}