package ua.onpu.repository;

import org.springframework.data.repository.CrudRepository;
import ua.onpu.entity.Task;

public interface TaskRepository extends CrudRepository<Task, Long> {
    Task findByTaskId(Long taskId);
}
