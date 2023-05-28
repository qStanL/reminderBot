package ua.onpu.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.onpu.repository.AssigmentRepository;
import ua.onpu.repository.TaskRepository;
import ua.onpu.repository.UserRepository;
import ua.onpu.service.DateParser;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataBaseControl {

    private UserRepository userRepository;

    private TaskRepository taskRepository;

    private AssigmentRepository assigmentRepository;
    @Autowired
    private DateParser dateParser;

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

    public Map<Long, User> getUsersMaps() {
        Map<Long, User> userMap = new HashMap<>();

        List<User> users = new ArrayList<>(userRepository.findAll());

        for (User user : users) {
            userMap.put(user.getChatId(), user);
        }

        return userMap;
    }

    public void setUsersMap(Map<Long, User> usersMap) {
        usersMap.forEach((aLong, user) -> userRepository.save(user));
    }

    public Long makeRemind(Message message, String group) throws DataAccessException {
        Task task = new Task();
        task.setTaskText(message.getText());
        task.setTaskGroup(group);
        taskRepository.save(task);

        Assigment assigment = new Assigment();

        userRepository.findById(message.getChatId())
                .ifPresent(u -> assigment.setUser(u));

        assigment.setTask(task);
        assigmentRepository.save(assigment);

        return task.getTaskId();
    }

    public List<Task> showList(Long chatId) throws DataAccessException {
        List<Assigment> assignments = assigmentRepository.findAllByUserChatId(chatId);
        List<Task> taskList = new ArrayList<>();
        for (Assigment a : assignments) {
            taskList.add(a.getTask());
        }

        return taskList;
    }

    public List<Task> showList(Long chatId, String group) throws DataAccessException {
        if (group.equals("ALL")) {
            return showList(chatId);
        }

        List<Assigment> assignments = assigmentRepository.findAllByUserChatId(chatId);
        List<Task> taskList = new ArrayList<>();

        for (Assigment a : assignments) {
            Task task = a.getTask();
            if (task.getTaskGroup() != null && task.getTaskGroup().equals(group)) {
                taskList.add(a.getTask());
            }
        }

        return taskList;
    }

    public void registerUser(User user) throws DataAccessException {
        if (userRepository.findById(user.getChatId()).isEmpty()) {
            userRepository.save(user);
        }
    }

    public void updateTask(String taskId, String text) throws DataAccessException {
        Task task = taskRepository.findByTaskId(Long.parseLong(taskId));
        task.setTaskText(text);

        taskRepository.save(task);
    }

    public void deleteTask(String taskId) throws DataAccessException {
        Task task = taskRepository.findByTaskId(Long.parseLong(taskId));
        Assigment assigment = assigmentRepository.findByTask(task);
        assigment.setUser(null);
        assigment.setTask(null);

        assigmentRepository.delete(assigment);
        taskRepository.delete(task);
    }

    public Set<String> groupList(Long chatId) {
        return assigmentRepository.findAllByUserChatId(chatId)
                .stream()
                .map(Assigment::getTask)
                .map(Task::getTaskGroup)
                .collect(Collectors.toSet());
    }

    public void createDeadline(User user, String dateInString) throws ParseException {
        Task task = taskRepository.findByTaskId(Long.valueOf(user.getTaskIdToManipulate()));

        Date date = dateParser.parse(dateInString);
        task.setTaskDeadline(date);
        taskRepository.save(task);
    }

    public List<Assigment> findByTaskTaskDeadlineIsNonNull() {
        List<Assigment> list = (List<Assigment>) assigmentRepository.findAll();

        return list.stream()
                .filter(a -> a.getTask().getTaskDeadline() != null)
                .collect(Collectors.toList());
    }

    public void completeDeadline(Task task) {
        Assigment a = assigmentRepository.findByTask(task);

        assigmentRepository.delete(a);
        taskRepository.delete(task);
    }
}
