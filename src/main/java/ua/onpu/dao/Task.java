package ua.onpu.dao;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private LocalDateTime taskDeadline;
    private String taskGroup;

}
