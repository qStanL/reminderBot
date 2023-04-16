package ua.onpu.process;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.onpu.handler.CallbackQuerryHandler;
import ua.onpu.handler.MessageHandler;

@Component
@AllArgsConstructor
public class ProcessorImpl implements Processor {

    private CallbackQuerryHandler callBackQuerryHandler;
    private MessageHandler messageHandler;


    @Override
    public void executeMessage(Message message) {
        messageHandler.choose(message);
    }

    @Override
    public void executeCallBackQuery(CallbackQuery callbackQuery) {
        callBackQuerryHandler.choose(callbackQuery);
    }
}
