package ua.onpu;


import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.checkerframework.checker.units.qual.A;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.onpu.configuration.BotConfiguration;
import ua.onpu.configuration.BotInit;
import ua.onpu.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfiguration configuration;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private AssigmentRepository assigmentRepository;
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
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            switch (messageText) {
                case "/start":
                    startCommand(chatId, EmojiParser.parseToUnicode("Hello, i'm ReminderBOT. Please choose the option" + ":blush:"));
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "Make a new reminder":
                    sendMessage(chatId, "Write your remind");
                    // TODO: Как то считать сообщение пользователя
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            sendMessage(update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getData());
        }
    }
    // Создание записи таска в БД
    private void makeRemind(Message message) {
        Task task = new Task();
        task.setTaskText(message.getText());
        taskRepository.save(task);

        Assigment assigment = new Assigment();

        if(userRepository.findById(message.getChatId()).isEmpty()){
            User user = new User();
            user.setChatId(message.getChatId());
            user.setUserName(message.getChat().getUserName());
            user.setRegisteredAt(LocalDateTime.now());

            userRepository.save(user);

            assigment.setUser(user);
            assigment.setTask(task);
            assigmentRepository.save(assigment);
        } else {
            User user = userRepository.findById(message.getChatId()).get();

            assigment.setUser(user);
            assigment.setTask(task);
            assigmentRepository.save(assigment);
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
