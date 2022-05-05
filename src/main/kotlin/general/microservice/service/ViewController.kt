package general.microservice.service

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class ViewController {

@RequestMapping("/")
@ResponseBody
fun mainPage(): String {
    return createHTML()
        .html {
            body {
                table {
                    thead() {
                        tr {
                            td {
                                +"Имя"
                            }
                            td {
                                +"Возраст"
                            }
                        }
                    }
                }
            }
        }
    }
}