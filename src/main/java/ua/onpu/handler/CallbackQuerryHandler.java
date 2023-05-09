package ua.onpu.handler;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ua.onpu.cache.Cache;
import ua.onpu.domain.Statements;
import ua.onpu.service.KeyboardService;
import ua.onpu.service.MessageService;
import ua.onpu.dao.DataBaseControl;
import ua.onpu.dao.User;

@Component
public class CallbackQuerryHandler implements Handler<CallbackQuery> {

    private final Cache<User> cache;
    private final MessageService messageService;
    private final DataBaseControl dataBaseControl;
    private final KeyboardService keyboardService;


    @Autowired
    public CallbackQuerryHandler(Cache<User> cache, MessageService messageService, DataBaseControl dataBaseControl, KeyboardService keyboardService) {
        this.cache = cache;
        this.messageService = messageService;
        this.dataBaseControl = dataBaseControl;
        this.keyboardService = keyboardService;
    }

    @Override
    public void choose(CallbackQuery callbackQuery) {
        User user = cache.findBy(callbackQuery.getMessage().getChatId());

        if (callbackQuery.getData().matches("^\\d+$")) {
            user.setTaskIdToManipulate(callbackQuery.getData());
        } else if (dataBaseControl.groupList(user.getChatId()).contains(callbackQuery.getData()) ||
                callbackQuery.getData().equals("ALL")) {
            user.setGroupToShow(callbackQuery.getData());
        } else {
            user.setState(Statements.valueOf(callbackQuery.getData()));
        }

        switch (user.getState()) {
            case VIEW_PROCESSING:
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Your reminder list")
                        .replyMarkup(keyboardService.viewProcessingStateKeyboard(dataBaseControl.showList(user.getChatId(), user.getGroupToShow())))
                        .build());

                user.setState(Statements.START);
                break;
            case EDIT:
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Select reminder that you want to edit")
                        .build());

                user.setState(Statements.EDIT_PROCESSING);
                break;
            case EDIT_PROCESSING:
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Write your updated reminder")
                        .replyMarkup(new ReplyKeyboardRemove(true))
                        .build());
                break;
            case DELETE:
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Select reminder that you want to delete")
                        .build());
                user.setState(Statements.DELETE_PROCESSING);
                break;
            case DELETE_PROCESSING:
                dataBaseControl.deleteTask(user.getTaskIdToManipulate());

                user.setState(Statements.VIEW);

                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Done!")
                        .replyMarkup(keyboardService.viewProcessingStateKeyboard(dataBaseControl.showList(user.getChatId(), user.getGroupToShow())))
                        .build());
                break;
            case EMPTY_LIST:
                user.setState(Statements.START);

                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(EmojiParser.parseToUnicode("Your reminder list is empty, add something"))
                        .replyMarkup(keyboardService.startStateKeyboard())
                        .build());
                break;
            case DEADLINE:
                user.setState(Statements.DEADLINE_PARSING);
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(EmojiParser.parseToUnicode("Choose reminder to set deadline"))
                        .build());
                break;
            case DEADLINE_PARSING:
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Write the date and time\n" +
                                "The following date formats are supported: \n'yyyy-MM-dd HH:mm:ss'")
                        .replyMarkup(new ReplyKeyboardRemove(true))
                        .build());
                break;
            case BACK:
                user.setState(Statements.START);

                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(EmojiParser.parseToUnicode("You have been returned to start. Please choose the option :blush:"))
                        .replyMarkup(keyboardService.startStateKeyboard())
                        .build());
                break;
            default:
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("First select the action below")
                        .build());
                break;
        }

    }

}
