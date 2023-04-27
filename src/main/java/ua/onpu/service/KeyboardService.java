package ua.onpu.service;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ua.onpu.dao.User;

public interface KeyboardService {

    ReplyKeyboard getKeyboard(User user);

}
