package ua.onpu.messagesender;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.onpu.TelegramBot;
import ua.onpu.messagesender.MessageSender;

@Log4j
@Service
class MessageSenderImpl implements MessageSender {

    private TelegramBot telegramBot;

    @Autowired
    public MessageSenderImpl(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendMessage(SendMessage sendMessage) {
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

}