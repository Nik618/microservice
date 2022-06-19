package general.microservice.service

import general.microservice.dto.valutes.list.all.ValCursAll
import general.microservice.dto.valutes.names.eng.Valuta
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

@Service
class DownloadService(

) {

    fun downloadValutesListAll(): ValCursAll? {
        val bis = BufferedInputStream(
            URL("https://www.cbr.ru/scripts/XML_daily.asp").openStream()
        )
        val serializer: Serializer = Persister()
        return serializer.read(
            ValCursAll::class.java, BufferedReader(InputStreamReader(bis)).lines().collect(
                Collectors.joining("\n")))
    }

    fun downloadValutesNamesEng(): Valuta? {
        val bis = BufferedInputStream(
            URL("https://www.cbr.ru/scripts/XML_val.asp").openStream()
        )
        val serializer: Serializer = Persister()
        return serializer.read(
            Valuta::class.java, BufferedReader(InputStreamReader(bis)).lines().collect(
                Collectors.joining("\n")))
    }

    fun downloadValutesListChosenValues(name : String): general.microservice.dto.valutes.list.chosen.values.ValCursChosenValues? {
        val bis = BufferedInputStream(
            URL("https://www.cbr.ru/scripts/XML_dynamic.asp" +
                    "?date_req1=${LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}" +
                    "&date_req2=${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}&VAL_NM_RQ=${name}")
                .openStream()
        )
        val serializer: Serializer = Persister()
        return serializer.read(
            general.microservice.dto.valutes.list.chosen.values.ValCursChosenValues::class.java, BufferedReader(InputStreamReader(bis)).lines().collect(
                Collectors.joining("\n")))
    }

}