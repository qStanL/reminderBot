package ua.onpu.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ua.onpu.messagesender.MessageSender;

@Component
public class CallbackQuerryHandler implements Handler<CallbackQuery> {

    private MessageSender messageSender;

    @Autowired
    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void choose(CallbackQuery callbackQuery) {
        messageSender.sendMessage(SendMessage.builder()
                .text("CALLBACK")
                .chatId(callbackQuery.getMessage().getChatId())
                .build());
        switch (callbackQuery.getData()) {

        }
    }
}
