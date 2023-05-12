package ua.onpu.repository;

import org.springframework.data.repository.CrudRepository;
import ua.onpu.entity.Assigment;
import ua.onpu.entity.Task;

import java.util.List;

public interface AssigmentRepository extends CrudRepository<Assigment, Long> {
    List<Assigment> findAllByUserChatId(Long chatId);

    Assigment findByTask(Task task);
}
