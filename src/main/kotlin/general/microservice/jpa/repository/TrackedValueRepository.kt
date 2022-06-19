package general.microservice.jpa.repository

import general.microservice.jpa.domain.TrackedValute
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackedValueRepository : CrudRepository<TrackedValute, Long> {
    fun findByNameAndChatId(name: String, chatId: Long): TrackedValute?
    fun findByChatId(chatId: Long): MutableIterable<TrackedValute?>
}