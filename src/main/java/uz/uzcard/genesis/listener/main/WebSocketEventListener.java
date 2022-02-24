package uz.uzcard.genesis.listener.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import uz.uzcard.genesis.dto.event.UserStateDto;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.entity._User;

import java.util.Objects;

@Component
@CrossOrigin
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private UserDao userDao;

    @Transactional
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
        String username = Objects.requireNonNull(event.getUser()).getName();
        if (username != null) {
            logger.info("User Connected : " + username);

            _User user = userDao.getByUseName(username);

            UserStateDto userState = new UserStateDto();
            userState.setUserName(username);

            messagingTemplate.convertAndSend("/topic/public", userState);
        }
    }

    @Transactional
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        String username = Objects.requireNonNull(event.getUser()).getName();
        if (username != null) {
            logger.info("User Disconnected : " + username);

            UserStateDto userState = new UserStateDto();
            userState.setUserName(username);

            messagingTemplate.convertAndSend("/topic/public", userState);
        }
    }
}
