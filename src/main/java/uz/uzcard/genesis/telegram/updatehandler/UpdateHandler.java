package uz.uzcard.genesis.telegram.updatehandler;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import uz.uzcard.genesis.telegram.processor.MessageProcessor;

/**
 * @author Javohir Elmurodov
 * @created 1/13/2021 | 3:46 PM
 * @project GTL
 */
public class UpdateHandler extends TelegramLongPollingBot {

    private final MessageProcessor processor;
    private final String token;
    private final String username;

    public UpdateHandler(boolean isServer, DefaultBotOptions botOptions, MessageProcessor processor, String token, String username) {
        super(!isServer ? botOptions:new DefaultBotOptions());
        this.processor = processor;
        this.token = token;
        this.username = username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        BotApiMethod<?> sendMessage = processor.process(update);
        try {
            if (sendMessage != null) {
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void executeAsync(SendMessage in) {
        try {
            executeAsync((SendMessage) in, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                }
            });
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

}

