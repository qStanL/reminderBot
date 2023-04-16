package ua.onpu.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ua.onpu.cache.Cache;
import ua.onpu.domain.Statements;
import ua.onpu.messagesender.MessageSender;
import ua.onpu.model.DataBaseControl;
import ua.onpu.model.User;

@Component
@AllArgsConstructor
public class CallbackQuerryHandler implements Handler<CallbackQuery> {

    private Cache<User> cache;
    private MessageSender messageSender;
    private DataBaseControl dataBaseControl;


    @Override
    public void choose(CallbackQuery callbackQuery) {
        User user = cache.findBy(callbackQuery.getMessage().getChatId());

        if (callbackQuery.getData().matches("^\\d+$")) {
            user.setTaskIdToManipulate(callbackQuery.getData());
            user.setState(Statements.EDIT_PROCESSING);
        } else {
            user.setState(Statements.valueOf(callbackQuery.getData()));
        }


        switch (user.getState()) {
            case EDIT:
                messageSender.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Select reminder that you want to edit")
                        .build());
                break;
            case EDIT_PROCESSING:
                messageSender.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Write your updated reminder")
                        .replyMarkup(new ReplyKeyboardRemove(true))
                        .build());
                break;
            case DELETE:
                messageSender.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Select reminder that you want to delete")
                        .build());
                break;
        }

    }
}
