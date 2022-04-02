package general.microservice.service

import com.google.gson.Gson
import general.microservice.pojos.ValCurs
import general.microservice.repository.MainRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController


@RestController
class MainService {

    @Autowired
    lateinit var repository: MainRepository

    var gson = Gson()

    @PostMapping("/api/1")
    fun greeting(@RequestBody request: String?) {

//        val parts: Array<MainRequest> = gson.fromJson(request, object : TypeToken<Array<MainRequest>>() {}.type)
//        parts.forEachIndexed {
//                firstValue, lastValue -> println("> Item ${firstValue}:\n${lastValue}")
//        }

        val serializer: Serializer = Persister()
        val valCurs = serializer.read(ValCurs::class.java, request)

        var budget = 10000000.0
        var countShares = 0
        var countUp = 0;
        var countDown = 0;
        //var up = false
        for (i in 0 until valCurs.list.size-2) {

            //(valCurs.list[i+2].value?.toDouble()!! - valCurs.list[i+3].value?.toDouble()!!)
//            if ((valCurs.list[i].value?.toDouble()!! > valCurs.list[i+1].value?.toDouble()!!)
//                && (valCurs.list[i+1].value?.toDouble()!! > valCurs.list[i+2].value?.toDouble()!!)
//                && (valCurs.list[i+2].value?.toDouble()!! < valCurs.list[i+3].value?.toDouble()!!))

            if ((valCurs.list[i].value?.toDouble()!! - valCurs.list[i+1].value?.toDouble()!!)/valCurs.list[i].value?.toDouble()!! > 0.1) {
                budget += countShares*valCurs.list[i+2].value?.toDouble()!!
                countShares = 0
            }

            if (countUp + countDown > 12) {
                budget += countShares*valCurs.list[i].value?.toDouble()!!
                countShares = 0
            }

            if (valCurs.list[i].value?.toDouble()!! > valCurs.list[i+1].value?.toDouble()!!) {
                countDown++

                if (countUp >= 3 && countShares != 0 && (valCurs.list[i].value?.toDouble()!! - valCurs.list[i+1].value?.toDouble()!!)/valCurs.list[i].value?.toDouble()!! > 0.02) {
                    countShares--
                    budget += valCurs.list[i+2].value?.toDouble()!!
                }

                countUp = 0

            }
            else {
                countUp++

                if (countDown >= 2) {
                    countShares++
                    budget -= valCurs.list[i+2].value?.toDouble()!!
                }

                countDown = 0
            }
        }
        println(budget)
        println(countShares)
    }
}