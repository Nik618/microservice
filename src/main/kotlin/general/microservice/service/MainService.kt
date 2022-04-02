package general.microservice.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import general.microservice.dto.MainRequest
import general.microservice.entities.MainEntity
import general.microservice.repository.MainRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController


@RestController
class MainService {

    @Autowired
    lateinit var repository: MainRepository

    var gson = Gson()

    @PostMapping("/api/1")
    fun greeting(@RequestBody request: String?) {
       // val mainRequest = gson.fromJson(request, MainRequest::class.java)

        val parts: Array<MainRequest> = gson.fromJson(request, object : TypeToken<Array<MainRequest>>() {}.type)
        parts.forEachIndexed {
                firstValue, lastValue -> println("> Item ${firstValue}:\n${lastValue}")
        }

//        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
//        val list: List<MainRequest> = listOf(
//            MainRequest(0, 15),
//            MainRequest(0, 16)
//        );
//        val jsonList: String = gson.toJson(list)
//        println(jsonList)

        //Assert.assertEquals(mainRequest.id, 1)
        //Assert.assertEquals(mainRequest.value, 120)


//        val mainEntity : MainEntity ?= null
//        mainEntity?.value = 100
//        repository.save(MainEntity(100))


    }

}