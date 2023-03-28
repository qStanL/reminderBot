package ua.onpu.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.onpu.cache.Cache;
import ua.onpu.domain.BotUser;
import ua.onpu.messagesender.MessageSender;

@Component
public class MessageHandler implements Handler<Message> {

    private final Cache<BotUser> cache;
    private final MessageSender messageSender;

    public MessageHandler(Cache<BotUser> cache, MessageSender messageSender) {
        this.cache = cache;
        this.messageSender = messageSender;
    }


    @Override
    public void choose(Message message) {
        BotUser user = cache.findBy(message.getChatId());
        // TODO: logic
    }
}
