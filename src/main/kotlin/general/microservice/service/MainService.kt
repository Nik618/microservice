package general.microservice.service

import general.microservice.bot.Bot
import general.microservice.entities.MainEntity
import general.microservice.pojos.current.ValCurs
import general.microservice.repository.MainRepository
import mu.KotlinLogging
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.util.stream.Collectors


@RestController
@EnableScheduling
class MainService {

    @Autowired
    lateinit var repository: MainRepository
    private val logger = KotlinLogging.logger {}
    private var giveValue = -1;
    private var url = URL("https://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=23/04/2022&date_req2=01/01/2100&VAL_NM_RQ=R01235")

    @PostMapping("/api/1")
    fun giveValue(@RequestBody request : Int): String? {
        giveValue = request
        logger.info { "Give value $request done" }
        return "OK"
    }

    @Scheduled(fixedDelay = 10000)
    fun getValue(): String {

        val mainEntities = repository.findAll()
        mainEntities.forEach() {
            logger.info { "Найдена запись ${it.name}" }
            url = URL("https://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=23/04/2022&date_req2=01/01/2100&VAL_NM_RQ=${it.name}")
            val bis = BufferedInputStream(url.openStream())
            val serializer: Serializer = Persister()
            val valCurs = serializer.read(ValCurs::class.java, BufferedReader(InputStreamReader(bis)).lines().collect(Collectors.joining("\n")))
            val value = valCurs.list[valCurs.list.size-1].value!!.replace(',', '.')
            bis.close()

        //repository.save(mainEntity)


        logger.info { "Request successful done" }
        if (value.toDouble() < it.value!!.toDouble()) {
            logger.info { "Condition met" }

            var urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s"
            val apiToken = "5306557210:AAGP184LM3Z_u0r2txzo-EMze_kzfec0oGg"
            val chatId = it.chatId //"1385518289"
            val text = "Цена достигла указанного значения! Текущая цена: $value"
            urlString = String.format(urlString, apiToken, chatId, text)
            val url = URL(urlString)
            val conn: URLConnection = url.openConnection()
            val sb = StringBuilder()
            val br = BufferedReader(InputStreamReader(BufferedInputStream(conn.getInputStream())))
            var inputLine: String? = ""
            while (br.readLine().also { inputLine = it } != null) {
                sb.append(inputLine)
            }
            //"OK $sb"
        } else {
            logger.error { "Condition not met" }
            //"ERROR"
        }
        }


        //val parts = gson.fromJson(request, Request::class.java)

//        var budget = 10000000.0
//        var countShares = 0
//        var countUp = 0;
//        var countDown = 0;
//
//        for (i in 0 until parts.close.size-2) {
//
//            if ((parts.close[i] - parts.close[i+1])/parts.close[i] > 0.1) {
//                budget += countShares*parts.close[i+2]
//                countShares = 0
//            }
//
//            if (countUp + countDown > 12) {
//                budget += parts.close[i]
//                countShares = 0
//            }
//
//            if (parts.close[i] > parts.close[i+1]) {
//                countDown++
//
//                if (countUp >= 3 && countShares != 0 && (parts.close[i] - parts.close[i])/parts.close[i+2] > 0.02) {
//                    countShares--
//                    budget += parts.close[i+2]
//                }
//
//                countUp = 0
//
//            }
//            else {
//                countUp++
//
//                if (countDown >= 2) {
//                    countShares++
//                    budget -= parts.close[i+2]
//                }
//
//                countDown = 0
//            }
//        }
//        println(budget)
//        println(countShares)
//        return Value(((budget + countShares * parts.close[parts.close.size-1])-10000000)/10000000*100)
        return "1"
    }
}