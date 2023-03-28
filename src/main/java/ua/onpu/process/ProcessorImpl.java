package ua.onpu.process;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.onpu.handler.CallbackQuerryHandler;
import ua.onpu.handler.MessageHandler;

@Component
public class ProcessorImpl implements Processor{

    private final CallbackQuerryHandler callBackQuerryHandler;
    private final MessageHandler messageHandler;

    public ProcessorImpl(CallbackQuerryHandler callBackQuerryHandler, MessageHandler messageHandler) {
        this.callBackQuerryHandler = callBackQuerryHandler;
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
