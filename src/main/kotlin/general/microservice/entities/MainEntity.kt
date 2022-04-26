package general.microservice.entities

import javax.persistence.*

@Entity
@Table(name="test_table")
open class MainEntity {

    constructor(_value : String): this() {
        this.value = _value
    }

    constructor()

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    open var id: Long? = null

    @Column(name = "value", nullable = false)
    open var value: String? = null
}