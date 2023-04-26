package ua.onpu.model.repository;

import org.springframework.data.repository.CrudRepository;
import ua.onpu.model.Assigment;
import ua.onpu.model.Task;

import java.util.List;

public interface AssigmentRepository extends CrudRepository<Assigment, Long> {
    List<Assigment> findAllByUserChatId(Long chatId);
    Assigment findByTask(Task task);
    Assigment findByUser_ChatId(Long chatId);
}
