package ua.onpu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.onpu.entity.Assigment;
import ua.onpu.entity.DataBaseControl;
import ua.onpu.service.MessageService;
import ua.onpu.service.RemindService;

import java.util.Date;
import java.util.List;

@Service
public class RemindServiceImpl implements RemindService {

    private final DataBaseControl dataBaseControl;
    private final MessageService messageService;

    @Autowired
    public RemindServiceImpl(DataBaseControl dataBaseControl, MessageService messageService) {
        this.dataBaseControl = dataBaseControl;
        this.messageService = messageService;
    }

    @Override
    @Scheduled(fixedRate = 60_000)
    public void remind() {
        List<Assigment> assigmentList = dataBaseControl.findByTaskTaskDeadlineIsNonNull();
        Date date = new Date();

        for (Assigment a : assigmentList) {
            if (a.getTask().getTaskDeadline().before(date)) {
                messageService.sendMessage(SendMessage.builder()
                        .chatId(a.getUser().getChatId())
                        .text("Deadline!\n" + a.getTask().getTaskText())
                        .build());

                dataBaseControl.completeDeadline(a.getTask());
            }
        }

    }
}
