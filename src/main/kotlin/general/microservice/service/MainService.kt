package general.microservice.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import general.microservice.dto.Request
import general.microservice.dto.Value
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
    fun greeting(@RequestBody request: String?): Value {


        val parts = gson.fromJson(request, Request::class.java)


//        val serializer: Serializer = Persister()
//        val valCurs = serializer.read(ValCurs::class.java, request)

        var budget = 10000000.0
        var countShares = 0
        var countUp = 0;
        var countDown = 0;

        for (i in 0 until parts.close.size-2) {

            if ((parts.close[i] - parts.close[i+1])/parts.close[i] > 0.1) {
                budget += countShares*parts.close[i+2]
                countShares = 0
            }

            if (countUp + countDown > 12) {
                budget += parts.close[i]
                countShares = 0
            }

            if (parts.close[i] > parts.close[i+1]) {
                countDown++

                if (countUp >= 3 && countShares != 0 && (parts.close[i] - parts.close[i])/parts.close[i+2] > 0.02) {
                    countShares--
                    budget += parts.close[i+2]
                }

                countUp = 0

            }
            else {
                countUp++

                if (countDown >= 2) {
                    countShares++
                    budget -= parts.close[i+2]
                }

                countDown = 0
            }
        }
        println(budget)
        println(countShares)
        return Value(((budget + countShares * parts.close[parts.close.size-1])-10000000)/10000000*100)
    }
}