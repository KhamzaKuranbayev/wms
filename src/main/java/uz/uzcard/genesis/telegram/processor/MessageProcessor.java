package uz.uzcard.genesis.telegram.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.hibernate.dao.TelegramGroupDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._TelegramGroups;
import uz.uzcard.genesis.uitls.LocaleMessage;
import uz.uzcard.genesis.uitls.ServerUtils;

/**
 * @author Javohir Elmurodov
 * @created 1/13/2021 | 3:48 PM
 * @project GTL
 */

@Service
public class MessageProcessor {
    private final LocaleMessage localeMessage;
    private final TelegramGroupDao telegramGroupDao;
    private final DepartmentDao departmentDao;

    @Autowired
    public MessageProcessor(LocaleMessage localeMessage,
                            TelegramGroupDao telegramGroupDao,
                            DepartmentDao departmentDao) {
        this.localeMessage = localeMessage;
        this.telegramGroupDao = telegramGroupDao;
        this.departmentDao = departmentDao;
    }

    @Transactional
    public BotApiMethod<?> process(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.isUserMessage() && "/start".equals(message.getText()))
                return new SendMessage(message.getChatId(),
                        localeMessage.getMessage("welcome", message.getFrom().getLanguageCode(), message.getFrom().getFirstName()));

            if (!message.isUserMessage() && message.getLeftChatMember() == null) {
                _TelegramGroups tgGroup = telegramGroupDao.getByChatID(message.getChatId());
                if (ServerUtils.isEmpty(tgGroup)) {
                    tgGroup = new _TelegramGroups();
                    tgGroup.setChatId(message.getChatId());
                    tgGroup.setGroupName(message.getChat().getTitle());
                    telegramGroupDao.save(tgGroup);
                }
            } else if (!message.isUserMessage()) {
                _TelegramGroups tgGroup = telegramGroupDao.getByChatID(message.getChatId());
                if (!ServerUtils.isEmpty(tgGroup)) {
                    tgGroup.setState(_State.DELETED);
                    telegramGroupDao.save(tgGroup);
                }
            }
        }
        return null;
    }
}
