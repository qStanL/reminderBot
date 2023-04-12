package ua.onpu.cache;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import ua.onpu.model.DataBaseControl;
import ua.onpu.model.User;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Component
@Log4j
public class UsersCache implements Cache<User> {

    private final Map<Long, User> users;
    @Autowired
    private DataBaseControl dataBaseControl;

    public UsersCache() {
        this.users = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        users.putAll(dataBaseControl.getUsersMaps());
    }

    @PreDestroy
    public void saveStatements() {
        dataBaseControl.setUsersMap(users);
    }

    @Override
    public void add(User user) {
        users.put(user.getChatId(), user);

        try {
            dataBaseControl.registerUser(user);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public User findBy(Long id) {
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}
