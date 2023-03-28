package ua.onpu.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotUser {

    private Long id;
    private String username;
    private Statements state;

}
