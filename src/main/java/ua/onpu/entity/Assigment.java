package ua.onpu.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity(name = "assigment")
@Getter
@Setter
@ToString
public class Assigment {
    @Id
    @GeneratedValue
    private long assigmentId;
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
