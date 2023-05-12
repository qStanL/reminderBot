package ua.onpu.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ua.onpu.domain.Statements;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "usersData")
@Getter
@Setter
@ToString
public class User {
    @Id
    private long chatId;
    private String userName;
    private Statements state;
    private String taskIdToManipulate;
    private String groupToCreate;
    private String groupToShow;
}
