package ua.onpu.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.onpu.handler.CallbackQuerryHandler;
import ua.onpu.handler.MessageHandler;

@Component
public class ProcessorImpl implements Processor {

    private CallbackQuerryHandler callBackQuerryHandler;
    private MessageHandler messageHandler;

    @Autowired
    public void setCallBackQuerryHandler(CallbackQuerryHandler callBackQuerryHandler){
        this.callBackQuerryHandler = callBackQuerryHandler;
    }
    @Autowired
    public void setMessageHandler(MessageHandler messageHandler){
        this.messageHandler = messageHandler;
    }

    @Override
    public void executeMessage(Message message) {
        messageHandler.choose(message);
    }

    @Override
    public void executeCallBackQuery(CallbackQuery callbackQuery) {
        callBackQuerryHandler.choose(callbackQuery);
    }
}
