package general.microservice.pojos.all

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Valuta", strict = false)
class Valuta {

    @field:ElementList(entry = "Item", inline = true)
    lateinit var list: List<Item>

}