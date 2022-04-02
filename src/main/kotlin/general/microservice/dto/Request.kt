package general.microservice.dto

//data class Request (
//    val Open: List<Int>,
//    val High: List<Int>,
//    val Low: List<Int>,
//    val Close: List<Int>,
//    val Volume: List<Int>,
//    val Date: List<Int>
//)

import com.beust.klaxon.*

data class Request (
    @Json(name = "Open")
    val open: List<Double>,

    @Json(name = "High")
    val high: List<Double>,

    @Json(name = "Low")
    val low: List<Double>,

    @Json(name = "Close")
    val close: List<Double>,

    @Json(name = "Volume")
    val volume: List<Double>,

    @Json(name = "Date")
    val date: List<Double>
)