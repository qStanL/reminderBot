package ua.onpu;


import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.onpu.configuration.BotConfiguration;
import ua.onpu.model.*;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfiguration configuration;
    @Autowired
    private DataBaseControl dataBaseControl;
    private Statements state = Statements.START;
    private static final String HELP_TEXT = "coming soon";

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
                    if(command.equals("/start")){
                        startCommand(chatId, EmojiParser.parseToUnicode("Hello, i'm ReminderBOT. Please choose the option" + ":blush:"));
                    }
                    else if (command.equals("Reminder list")){
                        state = Statements.VIEW;
                        dataBaseControl.showList(update.getMessage());
                    }
                    else if(command.equals("Make a new reminder")){
                        state = Statements.CREATE;
                        sendMessage(chatId, "Write your reminder");
                    }
                    else if(command.equals("Delete reminder")) {
                        state = Statements.DELETE_CONFIRMATION;
                        sendMessage(chatId, "Select the reminder you want to delete");
                        dataBaseControl.showList(update.getMessage());
                    } else {
                        startCommand(chatId, EmojiParser.parseToUnicode("Please choose the option" + ":blush:"));
                    }
                    break;
                case CREATE:
                    dataBaseControl.makeRemind(update.getMessage());
                    state = Statements.START;
                    break;
                case VIEW:
                    sendMessage(chatId, "VIEW");
                    break;
                case EDIT:
                    sendMessage(chatId, "EDIT");
                    break;
                case DELETE_CONFIRMATION:
                    sendMessage(chatId, "DELETE CONF");
                    break;
                case COMPLETE:
                    sendMessage(chatId, "COMPLETE");
                    break;
            }
        }
    }


    // Стартовые кнопки
    private void startCommand(String chatId, String text) {
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

        sendMessage(chatId, text, replyKeyboardMarkup);
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
