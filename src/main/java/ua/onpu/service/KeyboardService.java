package ua.onpu.service;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ua.onpu.dao.Task;
import java.util.List;
import java.util.Set;

public interface KeyboardService {

    ReplyKeyboard startStateKeyboard();
    ReplyKeyboard viewProcessingStateKeyboard(List<Task> list);
    ReplyKeyboard viewStateKeyboard(Set<String> group);


}
