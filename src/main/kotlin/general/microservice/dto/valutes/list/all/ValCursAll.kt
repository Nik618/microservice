package general.microservice.dto.valutes.list.all

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
class ValCursAll {

    @field:ElementList(entry = "Valute", inline = true)
    lateinit var list: List<Valute>

}