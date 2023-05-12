package ua.onpu.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.*;
import java.util.Date;

@Entity(name = "task")
@Getter
@Setter
@ToString
public class Task {
    @Id
    @GeneratedValue
    private long taskId;
    @Column(nullable = false)
    private String taskText;
    private Date taskDeadline;
    private String taskGroup;

}
