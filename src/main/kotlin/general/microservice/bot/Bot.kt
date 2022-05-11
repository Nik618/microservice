package general.microservice.bot

import general.microservice.entities.MainEntity
import general.microservice.pojos.all.ValCurs
import general.microservice.repository.MainRepository
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.Collectors


@Service
class Bot : TelegramLongPollingBot() {

    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    private var sendNumber = false
    private var startRemove = false

    lateinit var valCurs : ValCurs

    @Autowired
    lateinit var repository: MainRepository

    private var valute = ""
    private var idV = ""

    val list : MutableList<MutableList<String>> = mutableListOf()
    val listCurrents : MutableList<MutableList<String>> = mutableListOf()

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = botName

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            if (message.hasText()) {
                val messageText = message.text

                if (messageText.equals("/start"))
                    send1(chatId, messageText)

                if (startRemove) {
                    listCurrents.forEach() {
                        if (it.get(0).equals(messageText)) {
                            repository.delete(repository.findByNameAndChatId(messageText, chatId)!!)
                            send1(chatId, messageText)
                        }
                    }
                    listCurrents.clear()
                    startRemove = false
                }

                if (sendNumber) {
                    val responseMessage = SendMessage(chatId.toString(), "Отслеживание запущено!")
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Получить список курсов валют", "Что отслеживается сейчас?")
                        )
                    )
                    execute(responseMessage)

                    val mainEntity = MainEntity(valute, messageText, chatId)
                    repository.save(mainEntity)
                    sendNumber = false
                }

                if (list.isNotEmpty()) {
                    list.forEach() {
                        if (it.get(0).equals(messageText)) {
                            valCurs.list.forEach() {
                                if (it.name.equals(messageText))
                                    idV = it.id!!
                            }
                            valute = idV
                            sendNotificationForSend(chatId)
                            sendNumber = true
                        }
                    }
                    list.clear()
                } else {
                    if (messageText.startsWith("Получить список курсов валют"))
                        getValutes(chatId, messageText)
                    if  (messageText.startsWith("Что отслеживается сейчас?"))
                        getCurrent(chatId)
                }
            }
        }
    }

    private fun send1(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId.toString(), "...")
        responseMessage.enableMarkdown(true)
        // добавляем кнопки
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Получить список курсов валют", "Что отслеживается сейчас?")
            )
        )
        execute(responseMessage)
    }

    private fun sendNotification(chatId: Long, responseText: String, valCurs: ValCurs) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)
        valCurs.list.forEach() {
            list.add(mutableListOf(it.name.toString()))
        }
        responseMessage.replyMarkup = getReplyMarkup(
            list
        )
        execute(responseMessage)
    }

    private fun getReplyMarkup(allButtons: List<List<String>>): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = allButtons.map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        return markup
    }

    private fun getValutes(chatId : Long, responseText: String) : String {
        val url = URL("https://www.cbr.ru/scripts/XML_daily.asp?date_req=")
        val bis = BufferedInputStream(url.openStream())
        val serializer: Serializer = Persister()
        valCurs = serializer.read(
            ValCurs::class.java, BufferedReader(InputStreamReader(bis)).lines().collect(
                Collectors.joining("\n")))
        var responce = ""
        valCurs.list.forEach() {
            responce += "${it.name}\t\t\t\t${it.value!!.replace(',', '.')}\n"
        }

        sendNotification(chatId, responce, valCurs)

        return responce
    }

    private fun getCurrent(chatId : Long) {
        var responce = ""
        val mainEntities = repository.findByChatId(chatId)
        mainEntities.forEach() {
            responce += "${it?.name}\n"
            listCurrents.add(mutableListOf(it?.name!!))
        }
        responce += "\nВыберите, что перестать отслеживать:"

        val responseMessage = SendMessage(chatId.toString(), responce)

        val listCurrents = mutableListOf(listCurrents.get(0))
        listCurrents.add(mutableListOf("Назад"))

        responseMessage.replyMarkup = getReplyMarkup(
            listCurrents
        )

        responseMessage.enableMarkdown(true)

        startRemove = true
        execute(responseMessage)
    }

    private fun sendNotificationForSend(chatId: Long) {
        val responseMessage = SendMessage(chatId.toString(), "Введите цену:")
        responseMessage.enableMarkdown(true)
        // добавляем кнопки
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")
            )
        )
        execute(responseMessage)
    }
}