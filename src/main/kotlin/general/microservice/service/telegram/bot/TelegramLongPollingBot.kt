package general.microservice.service.telegram.bot

import general.microservice.jpa.domain.TrackedValute
import general.microservice.dto.valutes.list.all.ValCursAll
import general.microservice.dto.valutes.names.eng.Valuta
import general.microservice.jpa.repository.TrackedValueRepository
import general.microservice.service.DownloadService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class TelegramLongPollingBot(
    private val repository: TrackedValueRepository,
    private val downloadService: DownloadService,
) : TelegramLongPollingBot() {

    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    private var sendNumberLow = false
    private var sendNumberHigh = false
    private var startRemove = false
    private var sendName = false
    private var name = ""
    private var value = ""
    private var idV = ""
    private var messageText = ""
    private var chatId = 0L
    private var valueLow = ""
    private var valueHigh = ""
    private val listValutes: MutableList<MutableList<String>> = mutableListOf()
    private val listCurrents: MutableList<MutableList<String>> = mutableListOf()

    lateinit var valCursAll: ValCursAll
    lateinit var valuta: Valuta

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = botName

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            chatId = update.message.chatId
            messageText = update.message.text

            if (messageText == "/start") {
                toMainMenu(
                    chatId,
                    "Вас приветствуют на сервисе информирования по курсам валют!\n\nЗдесь вы можете указать интервал цены рубля по отношению к другой валюте (по данным ЦБ РФ) и программа будет регулярно проверять выход текущей цены за этот интервал.\n\nВыберите действие:"
                )
                return
            }
            if (messageText == "Назад") {
                back()
                return
            }
            if (messageText == "Получить список курсов валют") {
                getValutes(chatId)
                return
            }
            if (messageText == "Что отслеживается сейчас?") {
                getCurrent(chatId)
                return
            }

            if (startRemove) {
                delete()
                return
            }
            if (sendName) {
                sendName()
                return
            }
            if (sendNumberLow) {
                sendNumberLow()
                return
            }
            if (sendNumberHigh) {
                sendNumberHigh()
                return
            }
        }
    }

    private fun toMainMenu(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)

        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Получить список курсов валют", "Что отслеживается сейчас?")
            )
        )
        execute(responseMessage)
    }

    private fun createNotificationWithButtonBack(chatId: Long, text: String) {
        val responseMessage = SendMessage(chatId.toString(), text)
        responseMessage.enableMarkdown(true)

        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Назад")
            )
        )
        execute(responseMessage)
    }

    private fun createNotificationAfterGetListAllValutes(chatId: Long, responseText: String, valCursAll: ValCursAll) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)
        listValutes.add(mutableListOf("Назад"))
        valCursAll.list.forEach { external ->
            var name = ""
            valuta.list.forEach {
                if (it.id == external.id)
                    name = it.engName!!
            }
            listValutes.add(mutableListOf(name))
        }
        responseMessage.replyMarkup = getReplyMarkup(
            listValutes
        )

        execute(responseMessage)
    }

    private fun getValutes(chatId: Long): String {
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

        createNotificationAfterGetListAllValutes(chatId, responce, valCursAll)
        sendName = true
        return responce
    }

    private fun getCurrent(chatId: Long) {
        startRemove = true

        listCurrents.clear()
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

        listCurrents.add(mutableListOf("Назад"))

        responseMessage.replyMarkup = getReplyMarkup(
            listCurrents
        )

        responseMessage.enableMarkdown(true)

        if (listCurrents.size != 1)
            listCurrents.forEach() {
                var name = ""
                execute(responseMessage)
            }
    }

    fun delete() {
        startRemove = false
            valuta.list.forEach() { internal ->
                if (internal.engName.equals(messageText))
                    name = internal.id!!
            }
            if (it[0] == messageText) {
                repository.delete(repository.findByNameAndChatId(name, chatId)!!)
                toMainMenu(chatId, "Отслеживание данной валюты прекращено!")
            } else toMainMenu(chatId, "Выберите действие:")

    }

    fun sendNumberLow() {
        valueLow = messageText
        createNotificationWithButtonBack(chatId, "Теперь введите верхний порог цены:")
        sendNumberLow = false
        sendNumberHigh = true
    }

    fun sendNumberHigh() {
        valueHigh = messageText

        val valCursChosenValues = downloadService.downloadValutesListChosenValues(name)!!
        value = valCursChosenValues.list[valCursChosenValues.list.size - 1].value!!.replace(',', '.')

        val trackedValute = TrackedValute().apply {
            name = this@TelegramLongPollingBot.name
            value = this@TelegramLongPollingBot.value
            valueLow = this@TelegramLongPollingBot.valueLow
            valueHigh = this@TelegramLongPollingBot.valueHigh
            chatId = this@TelegramLongPollingBot.chatId
        }
        repository.save(trackedValute)
        sendNumberHigh = false

        toMainMenu(chatId, "Отслеживание запущено! Как только реальная цена выйдет за указанный диапазон, программа сообщит вам об этом.")
    }

    fun sendName() {
        sendName = false
        listValutes.forEach {
            if (it[0] == messageText) {
                valuta.list.forEach {
                    if (it.engName.equals(messageText))
                        idV = it.id!!
                }
                name = idV
                createNotificationWithButtonBack(chatId, "Вам будет предложено ввести нижний и верхний пороги цены. Как только реальная цена окажется за пределами этого интервала, программа сообщит вам об этом! \n" +
                        "\nВведите цену (нижний порог):")
                sendNumberLow = true
            }
        }
        listValutes.clear()
    }

    fun back() {
        sendNumberLow = false
        sendNumberHigh = false
        sendName = false
        toMainMenu(chatId, "Выберите действие:")
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

}