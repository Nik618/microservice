package general.microservice.dto.valutes.list.chosen.values

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
class ValCursChosenValues {

    @field:ElementList(entry = "Record", inline = true)
    lateinit var list: List<Record>

}