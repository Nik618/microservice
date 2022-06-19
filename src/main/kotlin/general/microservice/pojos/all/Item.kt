package general.microservice.pojos.all

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Item", strict = false)
class Item {

    @field:Attribute(name = "ID")
    var id: String ?= null

    @field:Element(name = "EngName", required = false)
    var engName: String ?= null

}