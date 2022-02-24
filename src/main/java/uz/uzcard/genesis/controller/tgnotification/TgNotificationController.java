package uz.uzcard.genesis.controller.tgnotification;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.telegram.NotificationService;

/**
 * @author Javohir Elmurodov
 * @created 1/15/2021 | 6:16 PM
 * @project GTL
 */

@Api(value = "TgNotification controller", description = "TgNotification")
@RestController
@RequestMapping(value = "/api")
public class TgNotificationController {

    private final NotificationService notificationService;

    @Autowired
    public TgNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/send-notification")
    public boolean sendTgNotification() {
        notificationService.sendAlarmAboutExpiringItems();
        return true;
    }
}
