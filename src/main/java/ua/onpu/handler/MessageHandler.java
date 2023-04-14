package ua.onpu.handler;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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
@Log4j
public class MessageHandler implements Handler<Message> {

    private final Cache<User> cache;
    private final MessageSender messageSender;
    private final DataBaseControl dataBaseControl;

    @Autowired
    public MessageHandler(Cache<User> cache, MessageSender messageSender, DataBaseControl dataBaseControl) {
        this.cache = cache;
        this.messageSender = messageSender;
        this.dataBaseControl = dataBaseControl;
    }

    @Override
    public void choose(Message message) {
        User user = cache.findBy(message.getChatId());
        if (user != null) {
            if (message.getText().equals("/start")) {
                cache.add(generateUserFromMessage(message));
                messageSender.sendMessage(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text(EmojiParser.parseToUnicode("Hello, i'm ReminderBOT. Please choose the option :blush:"))
                        .replyMarkup(startStateKeyboard())
                        .build());
            }
            switch (user.getState()) {

                case START:
                    String command = message.getText();
                    switch (command) {

                        case "Reminder list":
                            user.setState(Statements.VIEW);
                            messageSender.sendMessage(SendMessage.builder()
                                    .chatId(user.getChatId())
                                    .text("Your reminder list")
                                    .replyMarkup(viewStateKeyboard(dataBaseControl.showList(message)))
                                    .build());
                            break;
                        case "Make a new reminder":
                            user.setState(Statements.CREATE);
                            messageSender.sendMessage(SendMessage.builder()
                                    .chatId(user.getChatId())
                                    .text("Write your reminder")
                                    .replyMarkup(new ReplyKeyboardRemove(true))
                                    .build());
                            break;
                    }
                    break;
                case CREATE:
                    try {
                        dataBaseControl.makeRemind(message);
                    } catch (DataAccessException e) {
                        log.error(e.getMessage());
                    }
                    messageSender.sendMessage(SendMessage.builder()
                            .text(EmojiParser.parseToUnicode("Done! :blush:"))
                            .chatId(message.getChatId())
                            .replyMarkup(startStateKeyboard())
                            .build());
                    user.setState(Statements.START);
                    break;

            }
        }

    }

    private User generateUserFromMessage(Message message) {
        User user = new User();
        user.setUserName(message.getFrom().getUserName());
        user.setChatId(message.getChatId());
        user.setState(Statements.START);

        return user;
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

    private ReplyKeyboard viewStateKeyboard(List<Task> list) {
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

            inlineKeyboardButton.setText(i++ + ". " + t.getTaskText());
            inlineKeyboardButton.setCallbackData(Long.toString(t.getTaskId()));

            buttonList.add(inlineKeyboardButton);
            rowButton.add(buttonList);

        }

        List<InlineKeyboardButton> buttonList = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Update");
        button.setCallbackData("UPDATE");

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
