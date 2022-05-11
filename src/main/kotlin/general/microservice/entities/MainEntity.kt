package general.microservice.entities

import javax.persistence.*

@Entity
@Table(name="test_table")
open class MainEntity {

    constructor(_name : String, _value : String, _chatId: Long): this() {
        this.name = _name
        this.value = _value
        this.chatId = _chatId
    }

    constructor()

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "value", nullable = false)
    open var value: String? = null

    @Column(name = "chatid", nullable = false)
    open var chatId: Long? = null
}