package ua.onpu.model;

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
    private long taskId;
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String taskText;
    private LocalDateTime taskDeadline;


}
