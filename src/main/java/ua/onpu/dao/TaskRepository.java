package ua.onpu.dao;

import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {
    Task findByTaskId(Long taskId);
}
