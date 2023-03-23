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
    private static final String HELP_TEXT = "coming soon";

    public TelegramBot(BotConfiguration configuration) {
        this.configuration = configuration;

        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "start bot"));
        botCommands.add(new BotCommand("/list", "todo list"));
        botCommands.add(new BotCommand("/help", "how to use this bot"));
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

            String[] userMessage = update.getMessage().getText().split(" ");

            String command = userMessage[0];
            String userText = String.join(" ", Arrays.copyOfRange(userMessage, 1, userMessage.length));

            System.out.println(command);
            switch (command) {
                case "/start":
                    startCommand(chatId, EmojiParser.parseToUnicode("Hello, i'm ReminderBOT. Please choose the option" + ":blush:"));
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/newremind":
                    //dataBaseControl.makeRemind(update.getMessage(), userText);
                    dataBaseControl.taskList(update.getMessage());
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
