package ua.onpu.keyboardservice;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.onpu.domain.Statements;
import ua.onpu.model.DataBaseControl;
import ua.onpu.model.Task;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class KeyboardServiceImpl implements KeyboardService {

    private final DataBaseControl dataBaseControl;

    @Override
    public ReplyKeyboard getKeyboard(Statements state, Message message) {
        switch (state) {
            case VIEW:
                return viewStateKeyboard(dataBaseControl.showList(message));
            case START:
                return startStateKeyboard();
            default:
                return new ReplyKeyboardRemove(true);
        }
    }

    private ReplyKeyboard startStateKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("Make a new reminder");
        row.add("Reminder list");

        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }


    private ReplyKeyboard viewStateKeyboard(List<Task> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowButton = new ArrayList<>();

        if (list.isEmpty()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText("Empty");
            inlineKeyboardButton.setCallbackData("EMPTY_LIST");
            buttonList.add(inlineKeyboardButton);
            rowButton.add(buttonList);

            inlineKeyboardMarkup.setKeyboard(rowButton);
            return inlineKeyboardMarkup;
        }

        int i = 1;
        for (Task t : list) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText(i++ + ". " + t.getTaskText());
            inlineKeyboardButton.setCallbackData(Long.toString(t.getTaskId()));

            buttonList.add(inlineKeyboardButton);
            rowButton.add(buttonList);

        }

        List<InlineKeyboardButton> buttonList = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Edit");
        button.setCallbackData("EDIT");

        buttonList.add(button);

        button = new InlineKeyboardButton();
        button.setText("Delete");
        button.setCallbackData("DELETE");

        buttonList.add(button);
        rowButton.add(buttonList);

        List<InlineKeyboardButton> b = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();

        button1.setText(EmojiParser.parseToUnicode("Set deadline :watch:"));
        button1.setCallbackData("TIME");

        b.add(button1);
        rowButton.add(b);

        inlineKeyboardMarkup.setKeyboard(rowButton);
        return inlineKeyboardMarkup;
    }
}
