package general.microservice.repository

import general.microservice.entities.MainEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MainRepository : CrudRepository<MainEntity, Long> {
    fun findByValue(value: Int): MainEntity?
}