package general.microservice.pojos.all

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
class ValCurs {

    @field:ElementList(entry = "Valute", inline = true)
    lateinit var list: List<Valute>

}