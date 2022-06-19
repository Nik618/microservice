package general.microservice.service

import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

@Service
class SendService {

    fun sendToTelegram(apiToken: String, chatId : Long, engName : String, value : String) {
        val url = URL(String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s", apiToken, chatId, "Цена $engName достигла указанного значения! Текущая цена: $value").replace(" ", "%20"))
        val br = BufferedReader(InputStreamReader(BufferedInputStream(url.openConnection().getInputStream())))

        val sb = StringBuilder()
        var inputLine: String?
        while (br.readLine().also { inputLine = it } != null) {
            sb.append(inputLine)
        }
    }

}