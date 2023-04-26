package ua.onpu.keyboardservice;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ua.onpu.model.User;

public interface KeyboardService {

    ReplyKeyboard getKeyboard(User user);

}
