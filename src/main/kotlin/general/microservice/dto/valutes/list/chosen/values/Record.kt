package general.microservice.dto.valutes.list.chosen.values

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "Record", strict = false)
class Record {

    @field:Element(name = "Value", required = false)
    var value: String ?= null

}