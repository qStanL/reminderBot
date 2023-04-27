package ua.onpu.handler;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
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
import ua.onpu.service.MessageService;
import ua.onpu.dao.DataBaseControl;
import ua.onpu.dao.Task;
import ua.onpu.dao.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Log4j
public class MessageHandler implements Handler<Message> {

    private final Cache<User> cache;
    private final MessageService messageService;
    private final DataBaseControl dataBaseControl;


    public MessageHandler(Cache<User> cache, MessageService messageService, DataBaseControl dataBaseControl) {
        this.cache = cache;
        this.messageService = messageService;
        this.dataBaseControl = dataBaseControl;

    }

    @Override
    public void choose(Message message) {
        User user = cache.findBy(message.getChatId());

        if (user != null) {

            if (message.getText().equals("/start")) {
                user.setState(Statements.START);
                messageService.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(EmojiParser.parseToUnicode("Hello, i'm ReminderBot. Please choose the option :blush:"))
                        .replyMarkup(startStateKeyboard())
                        .build());
            }

            switch (user.getState()) {
                case START:
                case VIEW:
                    switch (message.getText()) {
                        case "Reminder list":
                            messageService.sendMessage(SendMessage.builder()
                                    .chatId(user.getChatId())
                                    .text("Write a group to view it(`all` - for all reminders)\nAvailable groups:\n"
                                            + dataBaseControl.groupList(user.getChatId()))
                                    .parseMode(ParseMode.MARKDOWN)
                                    .replyMarkup(new ReplyKeyboardRemove(true))
                                    .build());

                            user.setState(Statements.VIEW_PROCESSING);
                            break;
                        case "Make a new reminder":
                            user.setState(Statements.CREATE);

                            messageService.sendMessage(SendMessage.builder()
                                    .chatId(user.getChatId())
                                    .text("Write your reminder")
                                    .replyMarkup(new ReplyKeyboardRemove(true))
                                    .build());
                            break;
                    }
                    break;
                case VIEW_PROCESSING:
                    user.setGroupToShow(message.getText());
                    messageService.sendMessage(SendMessage.builder()
                            .chatId(user.getChatId())
                            .text("Your reminder list")
                            .replyMarkup(viewProcessingStateKeyboard(dataBaseControl.showList(user.getChatId(), user.getGroupToShow())))
                            .build());
                    user.setState(Statements.START);
                    break;
                case CREATE:
                    user.setTaskIdToManipulate(dataBaseControl.makeRemind(message).toString());

                    user.setState(Statements.CREATE_CONFIRMATION);
                    messageService.sendMessage(SendMessage.builder()
                            .text(EmojiParser.parseToUnicode("Done!:blush:\nWrite group for the task \nIf you don't want to write it, just copy it - `GENERAL`"))
                            .chatId(user.getChatId())
                            .replyMarkup(new ReplyKeyboardRemove(true))
                            .parseMode(ParseMode.MARKDOWN)
                            .build());
                    break;
                case CREATE_CONFIRMATION:
                    dataBaseControl.setGroup(message, user.getTaskIdToManipulate());

                    user.setState(Statements.START);
                    messageService.sendMessage(SendMessage.builder()
                            .text(EmojiParser.parseToUnicode("Done! :blush:"))
                            .chatId(user.getChatId())
                            .replyMarkup(startStateKeyboard())
                            .build());
                    break;
                case EDIT_PROCESSING:
                    dataBaseControl.updateTask(user.getTaskIdToManipulate(), message.getText());

                    user.setState(Statements.VIEW);
                    messageService.sendMessage(SendMessage.builder()
                            .chatId(user.getChatId())
                            .text("Done!")
                            .replyMarkup(viewProcessingStateKeyboard(dataBaseControl.showList(user.getChatId(), user.getGroupToShow())))
                            .build());
                    break;
            }
        } else {
            user = generateUserFromMessage(message);
            cache.add(user);

            messageService.sendMessage(SendMessage.builder()
                    .chatId(user.getChatId())
                    .text(EmojiParser.parseToUnicode("Hello, i'm ReminderBot. Please choose the option :blush:"))
                    .replyMarkup(startStateKeyboard())
                    .build());
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

        /*buttonList = new ArrayList<>();
        button = new InlineKeyboardButton();

        button.setText(EmojiParser.parseToUnicode("Set deadline :watch:("));
        button.setCallbackData("TIME");

        buttonList.add(button);
        rowButton.add(buttonList);*/

        buttonList = new ArrayList<>();
        button = new InlineKeyboardButton();

        button.setText("Back");
        button.setCallbackData("BACK");

        buttonList.add(button);
        rowButton.add(buttonList);

        inlineKeyboardMarkup.setKeyboard(rowButton);
        return inlineKeyboardMarkup;
    }

    private ReplyKeyboard viewStateKeyboard(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        Set<String> group = dataBaseControl.groupList(chatId);
        if (group.isEmpty()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText("Add at least one reminder");
            inlineKeyboardButton.setCallbackData("EMPTY_LIST");

            buttonList.add(inlineKeyboardButton);
            rowsInLine.add(buttonList);

            inlineKeyboardMarkup.setKeyboard(rowsInLine);
            return inlineKeyboardMarkup;
        }

        for (String g : group) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText("GENERAL");
            inlineKeyboardButton.setCallbackData("GENERAL");

            buttonList.add(inlineKeyboardButton);
            rowsInLine.add(buttonList);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }
}
