package general.microservice.repository

import general.microservice.jpa.MainEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MainRepository : CrudRepository<MainEntity, Long> {
    //fun findByValue(value: String): MainEntity?
    fun findByName(name: String): MainEntity?
    fun findByNameAndChatId(name: String, chatId: Long): MainEntity?
    fun findByChatId(chatId: Long): MutableIterable<MainEntity?>
}