package general.microservice.service

import general.microservice.dto.valutes.list.chosen.values.ValCursChosenValues
import general.microservice.jpa.domain.TrackedValute
import general.microservice.jpa.repository.TrackedValueRepository
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

@RestController
class ValuteService(
    private val downloadService: DownloadService,
    private var trackedValueRepository: TrackedValueRepository,
) {
    private var chatId = 0L
    private var urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s"
    private val apiToken = "5306557210:AAGx7I3230rLReD2CXRjI_Kc8XVKpI7d18c"
    private val logger = KotlinLogging.logger {}
    private var giveValue = -1;

    @PostMapping("/api/1")
    fun giveValue(@RequestBody request: Int): String? {
        giveValue = request
        logger.info { "Give value $request done" }
        return "OK"
    }

    fun checkCondition(entity: TrackedValute, valCursChosenValues: ValCursChosenValues) {
        val value = valCursChosenValues.list[valCursChosenValues.list.size-1].value!!.replace(',', '.')
        entity.value = value
        trackedValueRepository.save(entity)

        if ((entity.valueLow!!.toDouble() > value.toDouble()) || (value.toDouble() > entity.valueHigh!!.toDouble())) {
            logger.info { "Condition met" }
            chatId = entity.chatId!! //"1385518289"
            val valuta = downloadService.downloadValutesNamesEng()!!

            var engName = ""
            valCursChosenValues.list.forEach() { external ->
                valuta.list.forEach() { internal ->
                    if (internal.id == entity.name)
                        engName = internal.engName!!
                } }

            val text = "Цена $engName достигла указанного значения! Текущая цена: $value"
            urlString = String.format(urlString, apiToken, chatId, text)
            val url = URL(urlString)
            val sb = StringBuilder()
            val br = BufferedReader(
                InputStreamReader(
                    BufferedInputStream(
                        url).openConnection().getInputStream()
                    )
                )
            )

            var inputLine: String
            while (br.readLine().also { inputLine = it } != null) {
                sb.append(inputLine)
            }

        } else {
            logger.info { "Condition not met" }
        }
    }
}