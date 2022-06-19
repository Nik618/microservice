package general.microservice.service.bot

import general.microservice.jpa.domain.TrackedValute
import general.microservice.dto.valutes.list.all.ValCursAll
import general.microservice.dto.valutes.list.chosen.values.ValCursChosenValues
import general.microservice.dto.valutes.names.eng.Valuta
import general.microservice.jpa.repository.TrackedValueRepository
import general.microservice.service.DownloadService
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
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
class Bot(
    private val repository: TrackedValueRepository,
    private val downloadService: DownloadService
) : TelegramLongPollingBot() {

    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    private var sendNumberLow = false
    private var sendNumberHigh = false
    private var startRemove = false

    lateinit var valCursAll : ValCursAll
    lateinit var valuta : Valuta

    private var name = ""
    private var value = ""
    private var idV = ""
    private var chatId = 0L

    private var valueLow = ""
    private var valueHigh = ""

    val list : MutableList<MutableList<String>> = mutableListOf()
    val listCurrents : MutableList<MutableList<String>> = mutableListOf()

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = botName

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            chatId = message.chatId
            if (message.hasText()) {
                val messageText = message.text

                if (messageText.equals("/start"))
                    send1(chatId, "Вас приветствуют на сервисе информирования по курсам валют!\n\nЗдесь вы можете указать интервал цены рубля по отношению к другой валюте (по данным ЦБ РФ) и программа будет регулярно проверять выход текущей цены за этот интервал.\n\nВыберите действие:")

                if (startRemove) {
                    if (messageText.equals("Назад")) {
                        send1(chatId, "Выберите действие:")
                    }
                    if (listCurrents.size != 1)
                        listCurrents.forEach() {

                            var name = ""
                            valuta.list.forEach() { internal ->
                                if (internal.engName.equals(messageText))
                                    name = internal.id!!
                            }

                            if (it.get(0).equals(messageText)) {
                                repository.delete(repository.findByNameAndChatId(name, chatId)!!)
                                send1(chatId, "Отслеживание данной валюты прекращено!")
                            }
                        }
                    listCurrents.clear()
                    startRemove = false
                }

                if (sendNumberLow) {

                    if (messageText.equals("Назад")) {
                        sendNumberLow = false
                        sendNumberHigh = false
                        send1(chatId, "Выберите действие:")
                    } else {

                        if (!sendNumberHigh) {
                            sendNumberHigh = true
                            valueLow = messageText
                            if (messageText != "100500")
                                sendNotificationForSend(chatId, "Ура! Теперь введите верхний порог цены:")
                            else sendNotificationForSend(
                                chatId,
                                "Обнаружена критическая ошибка в программе. За последствия автор ответственности не несёт. Введите цену (верхний порог):"
                            )
                            return
                        }
                        valueHigh = messageText

                        val responseMessage = SendMessage(
                            chatId.toString(),
                            "Отслеживание запущено! Как только реальная цена выйдет за указанный диапазон, программа сообщит вам об этом."
                        )

                        responseMessage.replyMarkup = getReplyMarkup(
                            listOf(
                                listOf("Получить список курсов валют", "Что отслеживается сейчас?")
                            )
                        )
                        execute(responseMessage)

                        val url =
                            URL("https://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=23/04/2022&date_req2=01/01/2100&VAL_NM_RQ=${name}")
                        val bis = BufferedInputStream(url.openStream())
                        val serializer: Serializer = Persister()
                        val valCursChosenValues = serializer.read(
                            ValCursChosenValues::class.java,
                            BufferedReader(InputStreamReader(bis)).lines().collect(Collectors.joining("\n"))
                        )
                        value = valCursChosenValues.list[valCursChosenValues.list.size - 1].value!!.replace(',', '.')

                        val trackedValute = TrackedValute().apply {
                            name = this@Bot.name
                            value = this@Bot.value
                            valueLow = this@Bot.valueLow
                            valueHigh = this@Bot.valueHigh
                            chatId = this@Bot.chatId
                        }
                        repository.save(trackedValute)
                        sendNumberLow = false
                        sendNumberHigh = false
                    }
                }

                if (list.isNotEmpty()) {
                    list.forEach {

                        if (it[0] == "Назад") {
                            sendNumberLow = false
                            sendNumberHigh = false
                            send1(chatId, "Выберите действие:")
                        } else

                        if (it[0] == messageText) {
                            valuta.list.forEach {
                                if (it.engName.equals(messageText))
                                    idV = it.id!!
                            }
                            name = idV
                            sendNotificationForSend(chatId, "Вам будет предложено ввести нижний и верхний пороги цены. Как только реальная цена окажется за пределами этого интервала, программа сообщит вам об этом! \n" +
                                    "\nВведите цену (нижний порог):")
                            sendNumberLow = true
                        }
                    }
                    list.clear()
                } else {
                    if (messageText.startsWith("Получить список курсов валют"))
                        getValutes(chatId)
                    if  (messageText.startsWith("Что отслеживается сейчас?"))
                        getCurrent(chatId)
                }
            }
        }
    }

    private fun send1(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)

        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Получить список курсов валют", "Что отслеживается сейчас?")
            )
        )
        execute(responseMessage)
    }

    private fun sendNotification(chatId: Long, responseText: String, valCursAll: ValCursAll) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)
        list.add(mutableListOf("Назад"))
        valCursAll.list.forEach { external ->
            var name = ""
            valuta.list.forEach {
                if (it.id == external.id)
                    name = it.engName!!
            }
            list.add(mutableListOf(name))
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

    private fun getValutes(chatId : Long) : String {
        valCursAll = downloadService.downloadValutesListAll()!!
        valuta = downloadService.downloadValutesNamesEng()!!

        var responce = ""
        valCursAll.list.forEach() {
            var engName = ""
            valuta.list.forEach() { internal ->
                if (internal.id == it.id)
                    engName = internal.engName!!
            }
            responce += "${it.value!!.replace(',', '.')}\t\t\t\t${engName}\n"
        }

        sendNotification(chatId, responce, valCursAll)

        return responce
    }

    private fun getCurrent(chatId : Long) {
        var responce = ""
        val mainEntities = repository.findByChatId(chatId)

        valuta = downloadService.downloadValutesNamesEng()!!

        mainEntities.forEach() {
            var name = ""
            valuta.list.forEach() { internal ->
                if (internal.id == it?.name)
                    name = internal.engName!!
            }
            responce += "$name цена: ${it?.value}\nнижняя цена: ${it?.valueLow} / верхняя цена: ${it?.valueHigh}\n\n"
            listCurrents.add(mutableListOf(name))
        }

        responce += if (listCurrents.isNotEmpty())
            "Выберите, что перестать отслеживать."
        else "\nНет отслеживаемых валют!"

        val responseMessage = SendMessage(chatId.toString(), responce)

//        var listCurrentsNew : MutableList<MutableList<String>> = mutableListOf()
//        if (listCurrents.isNotEmpty())
//            listCurrentsNew = mutableListOf(listCurrents[0])
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

        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Назад")
            )
        )
        execute(responseMessage)
    }
}