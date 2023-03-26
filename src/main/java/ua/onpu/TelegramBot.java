package ua.onpu;


import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.onpu.configuration.BotConfiguration;
import ua.onpu.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfiguration configuration;

    private DataBaseControl dataBaseControl;
    private Statements state;
    private final Map<String, String> taskID;

    public TelegramBot(BotConfiguration configuration) {
        this.configuration = configuration;

        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "start bot"));
        validate(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        taskID = new HashMap<>();
        state = Statements.START;
    }

    @Autowired
    public void setDataBaseControl(DataBaseControl dataBaseControl) {
        this.dataBaseControl = dataBaseControl;
    }

    @Override
    public String getBotUsername() {
        return configuration.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return configuration.getBotToken();
    }


    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String command = update.getMessage().getText();

            switch (state) {
                case START:
                    switch (command) {
                        case "/start":
                            try {
                                dataBaseControl.registerUser(update.getMessage());
                            } catch (DataAccessException e) {
                                log.error(e.getMessage());
                            }
                            sendMessage(chatId, EmojiParser.parseToUnicode("Hello, i'm ReminderBOT. Please choose the option" + ":blush:"), startStateKeyboard());
                            break;
                        case "Reminder list":
                            state = Statements.VIEW;
                            sendMessage(chatId, "Your reminder list", viewStateKeyboard(dataBaseControl.showList(update)));
                            break;
                        case "Make a new reminder":
                            state = Statements.CREATE;
                            sendMessage(chatId, "Write your reminder", new ReplyKeyboardRemove(true));
                            break;
                        case "Delete reminder":
                            state = Statements.DELETE;
                            sendMessage(chatId, "Select the reminder you want to delete", viewStateKeyboard(dataBaseControl.showList(update)));
                            break;
                        default:
                            sendMessage(chatId, EmojiParser.parseToUnicode("Please choose the option" + ":blush:"), startStateKeyboard());
                            break;
                    }
                    break;
                case CREATE:
                    try {
                        dataBaseControl.makeRemind(update.getMessage());
                    } catch (DataAccessException e) {
                        log.error(e.getMessage());
                    }
                    sendMessage(chatId, "Done!", startStateKeyboard());
                    state = Statements.START;
                    break;
                case VIEW:
                    switch (command) {
                        case "Make a new reminder":
                            state = Statements.CREATE;
                            sendMessage(chatId, "Write your reminder", new ReplyKeyboardRemove(true));
                            break;
                        case "Reminder list":
                            sendMessage(chatId, "Your reminder list", viewStateKeyboard(dataBaseControl.showList(update)));
                            break;
                    }
                    break;
                case EDIT:
                    String taskID = this.taskID.get(chatId);
                    try {
                        dataBaseControl.updateTask(taskID, update.getMessage().getText());
                    } catch (DataAccessException e) {
                        log.error(e.getMessage());
                    }
                    sendMessage(chatId, "Done!", startStateKeyboard());
                    sendMessage(chatId, "Your updated list", viewStateKeyboard(dataBaseControl.showList(update)));
                    state = Statements.VIEW;
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            String callBack = update.getCallbackQuery().getData();

            switch (state) {
                case VIEW:
                    switch (callBack) {
                        case "EMPTY_LIST":
                            sendMessage(chatId, "Your list is empty, please add something", startStateKeyboard());
                            break;
                        case "UPDATE":
                            sendMessage(chatId, "Choose the reminder to update");
                            state = Statements.EDIT;
                            break;
                        case "DELETE":
                            sendMessage(chatId, "Choose the reminder to delete");
                            state = Statements.DELETE;
                            break;
                    }
                    break;
                case EDIT:
                    taskID.put(chatId, callBack);
                    sendMessage(chatId, "Write your updated reminder", new ReplyKeyboardRemove(true));
                    break;
                case DELETE:
                    try {
                        dataBaseControl.deleteTask(callBack);
                    } catch (DataAccessException e) {
                        log.error(e.getMessage());
                    }
                    sendMessage(chatId, "Your current reminder list", viewStateKeyboard(dataBaseControl.showList(update)));
                    state = Statements.VIEW;
                    break;
            }
        }
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

        inlineKeyboardMarkup.setKeyboard(rowButton);
        return inlineKeyboardMarkup;
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

    private void sendMessage(String chatId, String text, ReplyKeyboard keyboardButtons) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboardButtons);

        validate(message);
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        log.info("Answer to " + message.getChatId() + " : " + message.getText());

        validate(message);
    }

    private void validate(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void validate(SetMyCommands commands) {
        try {
            execute(commands);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

}
