package ua.onpu.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface MessageService {


    void sendMessage(SendMessage sendMessage);
    void sendMessage(EditMessageText sendMessage);
}
