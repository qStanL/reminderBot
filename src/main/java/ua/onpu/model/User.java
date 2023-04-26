package ua.onpu.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ua.onpu.domain.Statements;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity(name = "usersData")
@Getter
@Setter
@ToString
public class User {
    @Id
    private long chatId;
    private String userName;
    private LocalDateTime registeredAt;
    private Statements state;
    private String taskIdToManipulate;
    private String groupToShow;
}
