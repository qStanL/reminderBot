package ua.onpu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.onpu.dao.Assigment;
import ua.onpu.dao.DataBaseControl;
import ua.onpu.service.MessageService;
import ua.onpu.service.RemindService;

import java.util.Date;
import java.util.Iterator;
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
    @Scheduled(fixedRate = 1000)
    public void remind() {
        List<Assigment> assigmentList = dataBaseControl.findByTaskTaskDeadlineIsNonNull();
        Date date = new Date();
        Iterator<Assigment> iterator = assigmentList.iterator();

        while (iterator.hasNext()) {
            Assigment a = iterator.next();

            if (a.getTask().getTaskDeadline().before(date)) {
                messageService.sendMessage(SendMessage.builder()
                        .chatId(a.getUser().getChatId())
                        .text("DEADLINE " + a.getTask().getTaskText())
                        .build());
                dataBaseControl.completeDeadline(a.getTask());
            }
        }

    }
}
