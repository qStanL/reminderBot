package ua.onpu;

import org.springframework.beans.factory.annotation.Autowired;
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

    public void makeRemind(Message message, String remind) {
        Task task = new Task();
        task.setTaskText(remind);
        taskRepository.save(task);

        Assigment assigment = new Assigment();

        if (userRepository.findById(message.getChatId()).isEmpty()) {
            User user = new User();
            user.setChatId(message.getChatId());
            user.setUserName(message.getChat().getUserName());
            user.setRegisteredAt(LocalDateTime.now());

            userRepository.save(user);

            assigment.setUser(user);
            assigment.setTask(task);
            assigmentRepository.save(assigment);
        } else {
            User user = userRepository.findById(message.getChatId()).get();

            assigment.setUser(user);
            assigment.setTask(task);
            assigmentRepository.save(assigment);
        }
    }

    public void taskList(Message message){
        List<Assigment> assigments = assigmentRepository.findAllByUserChatId(message.getChatId());
        List<Task> taskList = new ArrayList<>();
        for(Assigment a : assigments){
            taskList.add(a.getTask());
        }

        System.out.println(taskList);
    }

}
