package ua.onpu.cache;

import org.springframework.stereotype.Component;
import ua.onpu.domain.BotUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UsersCache implements Cache<BotUser> {

    private final Map<Long, BotUser> users;

    public UsersCache() {
        this.users = new HashMap<>();
    }

    @Override
    public void add(BotUser botUser) {
        if (botUser.getId() != null) {
            users.put(botUser.getId(), botUser);
        }
    }

    @Override
    public BotUser findBy(Long id) {
        return users.get(id);
    }

    @Override
    public List<BotUser> getAll() {
        return new ArrayList<>(users.values());
    }
}
