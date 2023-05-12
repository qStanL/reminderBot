package ua.onpu.repository;

import org.springframework.data.repository.CrudRepository;
import ua.onpu.entity.User;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findAll();

}
