package ua.onpu.handler;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ua.onpu.cache.Cache;
import ua.onpu.domain.Statements;
import ua.onpu.service.KeyboardService;
import ua.onpu.service.MessageService;
import ua.onpu.dao.DataBaseControl;
import ua.onpu.dao.User;

@Component
@Log4j
public class MessageHandler implements Handler<Message> {

    private final Cache<User> cache;
    private final MessageService messageService;
    private final DataBaseControl dataBaseControl;
    private final KeyboardService keyboardService;


    public MessageHandler(Cache<User> cache, MessageService messageService, DataBaseControl dataBaseControl, KeyboardService keyboardService) {
        this.cache = cache;
        this.messageService = messageService;
        this.dataBaseControl = dataBaseControl;
        this.keyboardService = keyboardService;

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
                        .replyMarkup(keyboardService.startStateKeyboard())
                        .build());
            }

            switch (user.getState()) {
                case START:
                case VIEW:
                case VIEW_PROCESSING:
                    switch (message.getText()) {
                        case "Reminder list":
                            messageService.sendMessage(SendMessage.builder()
                                    .chatId(user.getChatId())
                                    .text("Choose group to view it\nAvailable groups:\n")
                                    .parseMode(ParseMode.MARKDOWN)
                                    .replyMarkup(keyboardService.viewStateKeyboard(dataBaseControl.groupList(user.getChatId())))
                                    .build());

                            user.setState(Statements.VIEW_PROCESSING);
                            break;
                        case "Make a new reminder":
                            user.setState(Statements.CREATE);

                            messageService.sendMessage(SendMessage.builder()
                                    .text(EmojiParser.parseToUnicode("Before create task write group for the task, please" +
                                            "\nIf you don't want to write it, just copy it - `GENERAL`"))
                                    .chatId(user.getChatId())
                                    .replyMarkup(new ReplyKeyboardRemove(true))
                                    .parseMode(ParseMode.MARKDOWN)
                                    .build());
                            break;
                    }
                    break;
                case CREATE:
                    user.setGroupToCreate(message.getText());

                    user.setState(Statements.CREATE_CONFIRMATION);
                    messageService.sendMessage(SendMessage.builder()
                            .chatId(user.getChatId())
                            .text("Now write your reminder")
                            .replyMarkup(new ReplyKeyboardRemove(true))
                            .build());
                    break;
                case CREATE_CONFIRMATION:
                    dataBaseControl.makeRemind(message, user.getGroupToCreate());

                    user.setState(Statements.START);
                    messageService.sendMessage(SendMessage.builder()
                            .text(EmojiParser.parseToUnicode("Done! :blush:"))
                            .chatId(user.getChatId())
                            .replyMarkup(keyboardService.startStateKeyboard())
                            .build());
                    break;
                case EDIT_PROCESSING:
                    dataBaseControl.updateTask(user.getTaskIdToManipulate(), message.getText());

                    messageService.sendMessage(SendMessage.builder()
                            .chatId(user.getChatId())
                            .text("Done!")
                            .replyMarkup(keyboardService.viewProcessingStateKeyboard(dataBaseControl.showList(user.getChatId(), user.getGroupToShow())))
                            .build());
                    user.setState(Statements.VIEW);
                    break;
                case DEADLINE_PARSING:
                    dataBaseControl.createDeadline(user, message.getText());

                    messageService.sendMessage(SendMessage.builder()
                            .chatId(user.getChatId())
                            .text("Done!")
                            .replyMarkup(new ReplyKeyboardRemove(true))
                            .build());

                    break;
            }
        } else {
            user = generateUserFromMessage(message);
            cache.add(user);

            messageService.sendMessage(SendMessage.builder()
                    .chatId(user.getChatId())
                    .text(EmojiParser.parseToUnicode("Hello, i'm ReminderBot. Please choose the option :blush:"))
                    .replyMarkup(keyboardService.startStateKeyboard())
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
