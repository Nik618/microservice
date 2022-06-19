package general.microservice.service

import general.microservice.dto.valutes.list.chosen.values.ValCursChosenValues
import general.microservice.jpa.domain.TrackedValute
import general.microservice.jpa.repository.TrackedValueRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ValuteService(
    private val downloadService: DownloadService,
    private val trackedValueRepository: TrackedValueRepository,
    private val sendService: SendService
) {
    private var chatId = 0L
    private val apiToken = "5306557210:AAGx7I3230rLReD2CXRjI_Kc8XVKpI7d18c"
    private val logger = KotlinLogging.logger {}

    fun checkCondition(entity: TrackedValute, valCursChosenValues: ValCursChosenValues) {
        val value = valCursChosenValues.list[valCursChosenValues.list.size-1].value!!.replace(',', '.')
        entity.value = value
        trackedValueRepository.save(entity)

        if ((entity.valueLow!!.toDouble() > value.toDouble()) || (value.toDouble() > entity.valueHigh!!.toDouble())) {
            logger.info { "Condition met" }
            chatId = entity.chatId!!
            val valuta = downloadService.downloadValutesNamesEng()!!

            var engName = ""
            valCursChosenValues.list.forEach() { external ->
                valuta.list.forEach() { internal ->
                    if (internal.id == entity.name)
                        engName = internal.engName!!
                }
            }

            sendService.sendToTelegram(apiToken, chatId, engName, value)

        } else {
            logger.info { "Condition not met" }
        }
    }
}