package general.microservice.entities

import javax.persistence.*

@Entity
@Table(name="test_table")
open class MainEntity {

    constructor(_name : String, _valueLow : String, _valueHigh : String, _chatId: Long): this() {
        this.name = _name
        this.valueLow = _valueLow
        this.valueHigh = _valueHigh
        this.chatId = _chatId
    }

    constructor()

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

    @Column(name = "chatid", nullable = false)
    open var chatId: Long? = null
}