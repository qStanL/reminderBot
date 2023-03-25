package ua.onpu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.onpu.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataBaseControl {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private AssigmentRepository assigmentRepository;

    public void makeRemind(Message message) throws DataAccessException {
        Task task = new Task();
        task.setTaskText(message.getText());
        taskRepository.save(task);

        Assigment assigment = new Assigment();

        User user = userRepository.findById(message.getChatId()).get();

        assigment.setUser(user);
        assigment.setTask(task);
        assigmentRepository.save(assigment);
    }

    public List<Task> showList(Message message) {
        List<Assigment> assigments = assigmentRepository.findAllByUserChatId(message.getChatId());
        List<Task> taskList = new ArrayList<>();
        for (Assigment a : assigments) {
            taskList.add(a.getTask());
        }

        return taskList;
    }

    public void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            User user = new User();
            user.setChatId(message.getChatId());
            user.setUserName(message.getChat().getUserName());
            user.setRegisteredAt(LocalDateTime.now());

            userRepository.save(user);
        }
    }

    public void updateTask(String taskID, String text) {
        Task task = taskRepository.findByTaskId(Long.parseLong(taskID));
        task.setTaskText(text);

        taskRepository.save(task);
    }
}
