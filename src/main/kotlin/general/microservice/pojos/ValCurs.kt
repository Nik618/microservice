package general.microservice.pojos

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
class ValCurs {

    @field:ElementList(entry = "Record", inline = true)
    lateinit var list: List<Record>

}