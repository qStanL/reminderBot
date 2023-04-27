package ua.onpu.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ua.onpu.dao.DataBaseControl;
import ua.onpu.dao.User;
import ua.onpu.service.KeyboardService;

@Service
public class KeyboardServiceImpl implements KeyboardService {

    private final DataBaseControl dataBaseControl;

    public KeyboardServiceImpl(DataBaseControl dataBaseControl) {
        this.dataBaseControl = dataBaseControl;
    }

    @Override
    public ReplyKeyboard getKeyboard(User user) {
        return new ReplyKeyboardRemove(true);
    }

}
