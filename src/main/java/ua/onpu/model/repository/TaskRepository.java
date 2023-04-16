package ua.onpu.model.repository;

import org.springframework.data.repository.CrudRepository;
import ua.onpu.model.Task;

public interface TaskRepository extends CrudRepository<Task, Long> {
    Task findByTaskId(Long taskId);
}
