package ua.onpu.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.onpu.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataBaseControl {

    private UserRepository userRepository;

    private TaskRepository taskRepository;

    private AssigmentRepository assigmentRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Autowired
    public void setAssigmentRepository(AssigmentRepository assigmentRepository) {
        this.assigmentRepository = assigmentRepository;
    }

    public void makeRemind(Message message) throws DataAccessException {
        Task task = new Task();
        task.setTaskText(message.getText());
        taskRepository.save(task);

        Assigment assigment = new Assigment();

        userRepository.findById(message.getChatId())
                .ifPresent(u -> assigment.setUser(u));

        assigment.setTask(task);
        assigmentRepository.save(assigment);
    }

    public List<Task> showList(Message message) throws DataAccessException {
        Long chatId = message.getChatId();

        List<Assigment> assigments = assigmentRepository.findAllByUserChatId(Long.parseLong(String.valueOf(chatId)));
        List<Task> taskList = new ArrayList<>();
        for (Assigment a : assigments) {
            taskList.add(a.getTask());
        }

        return taskList;
    }

    public List<Task> showList(CallbackQuery callbackQuery) throws DataAccessException {
        Long chatId = callbackQuery.getMessage().getChatId();

        List<Assigment> assigments = assigmentRepository.findAllByUserChatId(chatId);
        List<Task> taskList = new ArrayList<>();
        for (Assigment a : assigments) {
            taskList.add(a.getTask());
        }

        return taskList;
    }

    public void registerUser(User user) throws DataAccessException {
        if (userRepository.findById(user.getChatId()).isEmpty()) {
            user.setRegisteredAt(LocalDateTime.now());

            userRepository.save(user);
        }
    }

    public void updateTask(String taskID, String text) throws DataAccessException {
        Task task = taskRepository.findByTaskId(Long.parseLong(taskID));
        task.setTaskText(text);

        taskRepository.save(task);
    }

    public void deleteTask(String taskID) throws DataAccessException {
        Task task = taskRepository.findByTaskId(Long.parseLong(taskID));
        Assigment assigment = assigmentRepository.findByTask(task);
        assigment.setUser(null);
        assigment.setTask(null);

        assigmentRepository.delete(assigment);
        taskRepository.delete(task);
    }
}
