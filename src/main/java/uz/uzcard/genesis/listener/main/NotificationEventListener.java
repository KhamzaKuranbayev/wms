package uz.uzcard.genesis.listener.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.event.main.NotificationEveryOneEvent;
import uz.uzcard.genesis.listener.order.OrderEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificationEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventListener.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void sendMessageEveryOne(NotificationEveryOneEvent event) {
        LOGGER.info("Contract create or update event received: " + event);

        if (event.getUserNames() != null && event.getComment() != null) {
            Map<String, String> data = new LinkedHashMap<>();
            data.put("title", event.getTitle());
            data.put("comment", event.getComment());

            for (String userName : event.getUserNames()) {
                if (userName != null || userName != "") {
                    messagingTemplate.convertAndSendToUser(
                            userName, "/notification/reply", data);
                }
            }
        }
    }
}
