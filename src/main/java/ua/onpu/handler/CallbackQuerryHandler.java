package ua.onpu.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class CallbackQuerryHandler implements Handler<CallbackQuery>{

    @Override
    public void choose(CallbackQuery callbackQuery) {
        switch (callbackQuery.getData()){
            // TODO: logic
        }
    }
}
