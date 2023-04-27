package ua.onpu.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface MessageService {


    void sendMessage(SendMessage sendMessage);
}
