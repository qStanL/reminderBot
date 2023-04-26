package ua.onpu.keyboardservice;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.onpu.model.DataBaseControl;
import ua.onpu.model.Task;
import ua.onpu.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
