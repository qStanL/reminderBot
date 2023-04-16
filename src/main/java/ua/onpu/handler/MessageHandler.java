package ua.onpu.handler;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ua.onpu.cache.Cache;
import ua.onpu.domain.Statements;
import ua.onpu.keyboardservice.KeyboardService;
import ua.onpu.messagesender.MessageSender;
import ua.onpu.model.DataBaseControl;
import ua.onpu.model.User;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j
@AllArgsConstructor
public class MessageHandler implements Handler<Message> {

    private final Cache<User> cache;
    private final MessageSender messageSender;
    private final DataBaseControl dataBaseControl;
    private final KeyboardService keyboardService;


    @Override
    public void choose(Message message) {
        User user = cache.findBy(message.getChatId());

        if (user != null) {

            if (message.getText().equals("/start")) {
                user.setState(Statements.START);
                messageSender.sendMessage(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(EmojiParser.parseToUnicode("Hello, i'm ReminderBot. Please choose the option :blush:"))
                        .replyMarkup(keyboardService.getKeyboard(user.getState(), message))
                        .build());
            }

            switch (user.getState()) {
                case START:
                case VIEW:
                    switch (message.getText()) {

                        case "Reminder list":
                            user.setState(Statements.VIEW);
                            messageSender.sendMessage(SendMessage.builder()
                                    .chatId(user.getChatId())
                                    .text("Your reminder list")
                                    .replyMarkup(keyboardService.getKeyboard(user.getState(), message))
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

                    user.setState(Statements.START);
                    messageSender.sendMessage(SendMessage.builder()
                            .text(EmojiParser.parseToUnicode("Done! :blush:"))
                            .chatId(user.getChatId())
                            .replyMarkup(keyboardService.getKeyboard(user.getState(), message))
                            .build());
                    break;
                case EDIT_PROCESSING:
                    dataBaseControl.updateTask(user.getTaskIdToManipulate(), message.getText());

                    user.setState(Statements.VIEW);
                    messageSender.sendMessage(SendMessage.builder()
                            .chatId(user.getChatId())
                            .text("Done!")
                            .replyMarkup(keyboardService.getKeyboard(user.getState(), message))
                            .build());
                    break;
            }
        } else {
            cache.add(generateUserFromMessage(message));
            messageSender.sendMessage(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(EmojiParser.parseToUnicode("Hello, i'm ReminderBot. Please choose the option :blush:"))
                    .replyMarkup(keyboardService.getKeyboard(Statements.START, message))
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

}
