package general.microservice.pojos.current

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Record", strict = false)
class Record {

    @field:Element(name = "Value", required = false)
    var value: String ?= null

}