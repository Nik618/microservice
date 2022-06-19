package general.microservice.dto.valutes.list.all

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "Valute", strict = false)
class Valute {

    @field:Attribute(name = "ID")
    var id: String ?= null

    @field:Element(name = "CharCode", required = false)
    var name: String ?= null

    @field:Element(name = "Value", required = false)
    var value: String ?= null

}