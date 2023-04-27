package ua.onpu.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AssigmentRepository extends CrudRepository<Assigment, Long> {
    List<Assigment> findAllByUserChatId(Long chatId);
    Assigment findByTask(Task task);
    Assigment findByUser_ChatId(Long chatId);
}
