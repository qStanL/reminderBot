package ua.onpu.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.onpu.entity.Task;
import ua.onpu.service.KeyboardService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class KeyboardServiceImpl implements KeyboardService {

    public KeyboardServiceImpl() {
    }

    @Override
    public ReplyKeyboard startStateKeyboard() {
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

    @Override
    public ReplyKeyboard viewProcessingStateKeyboard(List<Task> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowButton = new ArrayList<>();

        if (list.isEmpty()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText("Add at least one reminder");
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

            inlineKeyboardButton.setText(i++ + ". " + t.getTaskText() + " -> " + t.getTaskGroup());
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

        buttonList = new ArrayList<>();
        button = new InlineKeyboardButton();

        button.setText("Deadline");
        button.setCallbackData("DEADLINE");

        buttonList.add(button);

        button = new InlineKeyboardButton();
        button.setText("Back");
        button.setCallbackData("BACK");

        buttonList.add(button);
        rowButton.add(buttonList);

        inlineKeyboardMarkup.setKeyboard(rowButton);
        return inlineKeyboardMarkup;
    }

    @Override
    public ReplyKeyboard viewProcessingStateKeyboardWithOutChangeButtons(List<Task> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowButton = new ArrayList<>();

        if (list.isEmpty()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText("Add at least one reminder");
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

            inlineKeyboardButton.setText(i++ + ". " + t.getTaskText() + " -> " + t.getTaskGroup());
            inlineKeyboardButton.setCallbackData(Long.toString(t.getTaskId()));

            buttonList.add(inlineKeyboardButton);
            rowButton.add(buttonList);
        }

        InlineKeyboardButton button = new InlineKeyboardButton();
        List<InlineKeyboardButton> buttonList = new ArrayList<>();
        button.setText("Cancel");
        button.setCallbackData("CANCEL");

        buttonList.add(button);
        rowButton.add(buttonList);

        inlineKeyboardMarkup.setKeyboard(rowButton);
        return inlineKeyboardMarkup;
    }

    @Override
    public ReplyKeyboard viewStateKeyboard(Set<String> group) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        if (group.isEmpty()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            inlineKeyboardButton.setText("Add at least one reminder");
            inlineKeyboardButton.setCallbackData("EMPTY_LIST");

            buttonList.add(inlineKeyboardButton);
            rowsInLine.add(buttonList);

            inlineKeyboardMarkup.setKeyboard(rowsInLine);
            return inlineKeyboardMarkup;
        }

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        List<InlineKeyboardButton> buttonList = new ArrayList<>();

        inlineKeyboardButton.setText("All groups");
        inlineKeyboardButton.setCallbackData("ALL");

        buttonList.add(inlineKeyboardButton);
        rowsInLine.add(buttonList);

        for (String g : group) {
            inlineKeyboardButton = new InlineKeyboardButton();
            buttonList = new ArrayList<>();

            inlineKeyboardButton.setText(g);
            inlineKeyboardButton.setCallbackData(g);

            buttonList.add(inlineKeyboardButton);
            rowsInLine.add(buttonList);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

}
