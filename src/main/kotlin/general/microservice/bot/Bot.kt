package general.microservice.bot

import general.microservice.entities.MainEntity
import general.microservice.pojos.all.ValCurs
import general.microservice.repository.MainRepository
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

    private var sendNumberLow = false
    private var sendNumberHigh = false

    private var startRemove = false

    lateinit var valCurs : ValCurs

    @Autowired
    lateinit var repository: MainRepository

    private var valute = ""
    private var idV = ""

    private var valueLow = ""
    private var valueHigh = ""

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
                    send1(chatId, "Вас приветствуют на сервисе информирования по курсам валют!\n\nЗдесь вы можете указать интервал цены рубля по отношению к другой валюте (по данным ЦБ РФ) и программа будет регулярно проверять вхождение текущей цены в этот интервал.\n\nВыберите действие:")

                if (startRemove) {
                    if (messageText.equals("Назад")) {
                        send1(chatId, "Выберите действие:")
                    }
                    if (listCurrents.size != 1)
                        listCurrents.forEach() {
                            if (it.get(0).equals(messageText)) {
                                repository.delete(repository.findByNameAndChatId(messageText, chatId)!!)
                                send1(chatId, "Отслеживание данной валюты прекращено!")
                            }
                        }
                    listCurrents.clear()
                    startRemove = false
                }

                if (sendNumberLow) {
                    if (!sendNumberHigh) {
                        sendNumberHigh = true
                        valueLow = messageText
                        if (messageText != "100500")
                            sendNotificationForSend(chatId, "Ура! Теперт введите верхний порог цены:")
                        else sendNotificationForSend(chatId, "Обнаружена критическая ошибка в программе. За последствия автор ответственности не несёт. Введите цену (верхний порог):")
                        return
                    }
                    valueHigh = messageText

                    var responseMessage = SendMessage(chatId.toString(), "Отслеживание запущено! Как только реальная цена попадёт в выбранный диапазон, программа сообщит вам об этом.")

                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Получить список курсов валют", "Что отслеживается сейчас?")
                        )
                    )
                    execute(responseMessage)

                    val mainEntity = MainEntity(valute, valueLow, valueHigh, chatId)
                    repository.save(mainEntity)
                    sendNumberLow = false
                    sendNumberHigh = false
                }

                if (list.isNotEmpty()) {
                    list.forEach() {
                        if (it.get(0).equals(messageText)) {
                            valCurs.list.forEach() {
                                if (it.name.equals(messageText))
                                    idV = it.id!!
                            }
                            valute = idV
                            sendNotificationForSend(chatId, "Вам будет предложено ввести нижний и верхний пороги цены. Как только реальная цена окажется в этом интервале, программа сообщит вам об этом! \n" +
                                    "\nВведите цену (нижний порог):")
                            sendNumberLow = true
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
        val responseMessage = SendMessage(chatId.toString(), responseText)
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

        responce += if (listCurrents.isNotEmpty())
            "\nВыберите, что перестать отслеживать."
        else "\nНет отслеживаемых валют!"


        val responseMessage = SendMessage(chatId.toString(), responce)

        var listCurrentsNew : MutableList<MutableList<String>> = mutableListOf()
        if (listCurrents.isNotEmpty())
            listCurrentsNew = mutableListOf(listCurrents.get(0))
        listCurrents.add(mutableListOf("Назад"))

        responseMessage.replyMarkup = getReplyMarkup(
            listCurrents
        )

        responseMessage.enableMarkdown(true)

        startRemove = true
        execute(responseMessage)
    }

    private fun sendNotificationForSend(chatId: Long, text : String) {
        val responseMessage = SendMessage(chatId.toString(), text)
        responseMessage.enableMarkdown(true)
        // добавляем кнопки
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("100")
            )
        )
        execute(responseMessage)
    }
}