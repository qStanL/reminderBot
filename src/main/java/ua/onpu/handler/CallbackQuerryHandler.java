package ua.onpu.handler;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.onpu.cache.Cache;
import ua.onpu.domain.Statements;
import ua.onpu.messagesender.MessageSender;
import ua.onpu.model.DataBaseControl;
import ua.onpu.model.Task;
import ua.onpu.model.User;
import java.util.ArrayList;
import java.util.List;

@Component
public class CallbackQuerryHandler implements Handler<CallbackQuery> {

    private final Cache<User> cache;
    private final MessageSender messageSender;
    private final DataBaseControl dataBaseControl;

    @Autowired
    public CallbackQuerryHandler(Cache<User> cache, MessageSender messageSender, DataBaseControl dataBaseControl) {
        this.cache = cache;
        this.messageSender = messageSender;
        this.dataBaseControl = dataBaseControl;
    }

    @Override
    public void choose(CallbackQuery callbackQuery) {
        User user = cache.findBy(callbackQuery.getMessage().getChatId());

        if (callbackQuery.getData().matches("^\\d+$")) {
            user.setTaskIdToManipulate(callbackQuery.getData());
        } else {
            user.setState(Statements.valueOf(callbackQuery.getData()));
        }


        switch (user.getState()) {
            case EDIT:
                messageSender.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Select reminder that you want to edit")
                        .build());
                user.setState(Statements.EDIT_PROCESSING);
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
                user.setState(Statements.DELETE_PROCESSING);
                break;
            case DELETE_PROCESSING:
                // FIXME: 26.04.2023
                dataBaseControl.deleteTask(user.getTaskIdToManipulate());
                user.setState(Statements.VIEW);
                messageSender.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Done!")
                        .replyMarkup(viewProcessingStateKeyboard(dataBaseControl.showList(user.getChatId())))
                        .build());
                break;
            case EMPTY_LIST:
                user.setState(Statements.START);

                messageSender.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(EmojiParser.parseToUnicode("Your reminder list is empty, add something first"))
                        .replyMarkup(startStateKeyboard())
                        .build());
                break;
        }

    }

    private ReplyKeyboard startStateKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("Make a new reminder");
        row.add("Reminder list");

        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboard viewProcessingStateKeyboard(List<Task> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowButton = new ArrayList<>();

        if (list.isEmpty()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText("Empty");
            inlineKeyboardButton.setCallbackData("EMPTY_LIST");
            buttonList.add(inlineKeyboardButton);
            rowButton.add(buttonList);

            inlineKeyboardMarkup.setKeyboard(rowButton);
            return inlineKeyboardMarkup;
        }

        int i = 1;
        for (Task t : list) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText(i++ + ". " + t.getTaskText() + " -> " + t.getTaskGroup());
            inlineKeyboardButton.setCallbackData(Long.toString(t.getTaskId()));

            buttonList.add(inlineKeyboardButton);
            rowButton.add(buttonList);
        }

        List<InlineKeyboardButton> buttonList = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Edit");
        button.setCallbackData("EDIT");

        buttonList.add(button);

        button = new InlineKeyboardButton();
        button.setText("Delete");
        button.setCallbackData("DELETE");

        buttonList.add(button);
        rowButton.add(buttonList);

        List<InlineKeyboardButton> b = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();

        button1.setText(EmojiParser.parseToUnicode("Set deadline :watch:"));
        button1.setCallbackData("TIME");

        b.add(button1);
        rowButton.add(b);

        inlineKeyboardMarkup.setKeyboard(rowButton);
        return inlineKeyboardMarkup;
    }

}
