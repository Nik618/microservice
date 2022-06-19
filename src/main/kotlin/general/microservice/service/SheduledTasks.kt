package general.microservice.service

import general.microservice.jpa.repository.TrackedValueRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class SheduledTasks(
    private var trackedValueRepository: TrackedValueRepository,
    private val downloadService: DownloadService,
    private val valuteService: ValuteService
) {

    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedDelay = 20000)
    fun checkCondition() {
        val mainEntities = trackedValueRepository.findAll()
        mainEntities.forEach() { it ->
            logger.info { "Найдена запись ${it.name}" }
            val valCurs = downloadService.downloadValutesListChosenValues(it.name!!)!!
            logger.info { "Request successful done" }
            valuteService.checkCondition(it, valCurs)
        }
    }
}

