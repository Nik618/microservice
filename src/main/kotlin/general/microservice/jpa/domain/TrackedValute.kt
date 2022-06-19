package general.microservice.jpa.domain

import javax.persistence.*

@Entity
@Table(name="tracked_valute")
open class TrackedValute {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "valuelow", nullable = false)
    open var valueLow: String? = null

    @Column(name = "valuehigh", nullable = false)
    open var valueHigh: String? = null

    @Column(name = "value", nullable = false)
    open var value: String? = null

    @Column(name = "chatid", nullable = false)
    open var chatId: Long? = null
}