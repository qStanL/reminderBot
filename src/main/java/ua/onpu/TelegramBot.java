package ua.onpu;


import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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
import java.util.List;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfiguration configuration;
    @Autowired
    private DataBaseControl dataBaseControl;
    private Statements state = Statements.START;
    private StringBuilder taskID;

    public TelegramBot(BotConfiguration configuration) {
        this.configuration = configuration;

        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "start bot"));
        validate(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
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
                            dataBaseControl.registerUser(update.getMessage());
                            sendMessage(chatId, EmojiParser.parseToUnicode("Hello, i'm ReminderBOT. Please choose the option" + ":blush:"), startStateKeyboard());
                            break;
                        case "Reminder list":
                            state = Statements.VIEW;
                            sendMessage(chatId, "Your reminder list", viewStateKeyboard(dataBaseControl.showList(update.getMessage())));
                            break;
                        case "Make a new reminder":
                            state = Statements.CREATE;
                            sendMessage(chatId, "Write your reminder", new ReplyKeyboardRemove(true));
                            break;
                        case "Delete reminder":
                            state = Statements.DELETE;
                            sendMessage(chatId, "Select the reminder you want to delete", viewStateKeyboard(dataBaseControl.showList(update.getMessage())));
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
                            Message message = update.getMessage();
                            System.out.println(message.getText());
                            break;
                    }
                    break;
                case EDIT:
                    dataBaseControl.updateTask(String.valueOf(taskID), update.getMessage().getText());
                    sendMessage(chatId, "Your updated list", viewStateKeyboard(dataBaseControl.showList(update.getMessage())));
                    state = Statements.START;
                    break;
                case DELETE:
                    sendMessage(chatId, "DELETE CONF");
                    break;
                case COMPLETE:
                    sendMessage(chatId, "COMPLETE");
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            String callBack = update.getCallbackQuery().getData();

            System.out.println(update.getCallbackQuery().getData());
            switch (state) {
                case VIEW:
                    switch (callBack) {
                        case "UPDATE":
                            sendMessage(chatId, "Choose the reminder to update");
                            state = Statements.EDIT;
                            break;
                        case "DELETE":
                            sendMessage(chatId, "Choose the reminder to delete");
                            state = Statements.DELETE;
                            break;
                        default:
                            sendMessage(chatId, "Unknown command", startStateKeyboard());
                            state = Statements.START;
                            break;
                    }
                    break;
                case EDIT:
                    taskID = new StringBuilder().append(callBack);
                    sendMessage(chatId, "Write yours updated reminder", new ReplyKeyboardRemove(true));
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

            inlineKeyboardButton.setText("EMPTY");
            inlineKeyboardButton.setCallbackData("EMPTY LIST");
            buttonList.add(inlineKeyboardButton);
            rowButton.add(buttonList);
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

        row = new KeyboardRow();

        row.add("Delete reminder");
        row.add("****");

        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

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
