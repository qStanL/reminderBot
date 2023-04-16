package ua.onpu;



import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.onpu.configuration.BotConfiguration;
import ua.onpu.process.Processor;
import java.util.ArrayList;
import java.util.List;


@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfiguration configuration;
    private final Processor processor;

    @Autowired
    public TelegramBot(BotConfiguration configuration, Processor processor) {
        this.configuration = configuration;
        this.processor = processor;

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
        processor.process(update);
    }

    private void validate(SetMyCommands commands) {
        try {
            execute(commands);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

}
