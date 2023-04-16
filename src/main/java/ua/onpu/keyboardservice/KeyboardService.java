package ua.onpu.keyboardservice;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ua.onpu.domain.Statements;

public interface KeyboardService {

    ReplyKeyboard getKeyboard(Statements state, Message message);

}
