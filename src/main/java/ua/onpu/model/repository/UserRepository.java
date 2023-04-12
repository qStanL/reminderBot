package ua.onpu.model.repository;

import org.springframework.data.repository.CrudRepository;
import ua.onpu.model.User;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findAll();

}
