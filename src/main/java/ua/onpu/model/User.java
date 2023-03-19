package ua.onpu.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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


}
