package uz.uzcard.genesis.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.uzcard.genesis.telegram.processor.MessageProcessor;
import uz.uzcard.genesis.telegram.updatehandler.UpdateHandler;

import javax.annotation.PostConstruct;

/**
 * @author Javohir Elmurodov
 * @created 1/13/2021 | 3:38 PM
 * @project GTL
 */

@Service
public class TelegramService {

    private UpdateHandler updateHandler;
    private final MessageProcessor processor;
    @Value("${tg.bot.token}")
    private String token;
    @Value("${tg.bot.username}")
    private String username;
    @Value("${is.server}")
    private boolean isServer;

    @Autowired
    public TelegramService(MessageProcessor processor) {
        this.processor = processor;
    }

    public void sendAsync(SendMessage message) {
        updateHandler.executeAsync(message);
    }

    public void sendSync(SendMessage message) {
        updateHandler.executeMessage(message);
    }


    @PostConstruct
    public void run() {
        ApiContextInitializer.init();
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
//        botOptions.setProxyHost("172.17.9.31");
//        botOptions.setProxyPort(8080);
//        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
        updateHandler = new UpdateHandler(isServer, botOptions, processor, token, username);
//        TelegramBotsApi botsApi = new TelegramBotsApi();
//        try {
//            botsApi.registerBot(updateHandler);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
    }

}
