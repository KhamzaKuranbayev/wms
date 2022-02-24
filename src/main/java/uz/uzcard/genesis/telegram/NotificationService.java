package uz.uzcard.genesis.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import uz.uzcard.genesis.hibernate.dao.PartitionDao;
import uz.uzcard.genesis.hibernate.dao.TelegramGroupDao;
import uz.uzcard.genesis.hibernate.entity._TelegramGroups;
import uz.uzcard.genesis.telegram.enums.Emojis;
import uz.uzcard.genesis.uitls.LocaleMessage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Javohir Elmurodov
 * @created 1/13/2021 | 4:07 PM
 * @project GTL
 */

@Service
public class NotificationService {
    private final PartitionDao partitionDao;
    private final TelegramGroupDao telegramGroupDao;
    private final LocaleMessage localeMessage;
    private final TelegramService telegramService;

    @Autowired
    public NotificationService(
            PartitionDao partitionDao,
            TelegramGroupDao telegramGroupDao,
            LocaleMessage localeMessage, TelegramService telegramService) {
        this.partitionDao = partitionDao;
        this.telegramGroupDao = telegramGroupDao;
        this.localeMessage = localeMessage;
        this.telegramService = telegramService;
    }

    @Scheduled(cron = "0 10 11 * * ?")
    public void sendNotification() {
        sendAlarmAboutExpiringItems();
    }

    public void sendAlarmAboutExpiringItems() {


        for (_TelegramGroups group : telegramGroupDao.getAll()) {
            if (group.getDepartmentId() != null) {
                List<String> productNames = partitionDao.getAllPartitions(group.getDepartmentId());
                for (int i = 0; i < productNames.size(); i = i + 20) {
                    List<String> string = productNames.stream().skip(i).limit(20).collect(Collectors.toList());
                    String body = Emojis.RED_CIRCLE + String.join("\n" + Emojis.RED_CIRCLE, string);
                    SendMessage message = new SendMessage();
                    message.setChatId(group.getChatId());
                    message.setText(body);
                    telegramService.sendSync(message);
                }
            }
        }
    }
}