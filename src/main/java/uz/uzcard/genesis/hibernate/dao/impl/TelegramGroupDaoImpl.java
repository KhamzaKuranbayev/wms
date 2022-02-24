package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.TelegramGroupDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._TelegramGroups;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Javohir Elmurodov
 * @created 1/14/2021 | 10:48 AM
 * @project GTL
 */

@Component(value = "telegramGroupDao")
public class TelegramGroupDaoImpl extends DaoImpl<_TelegramGroups> implements TelegramGroupDao {
    public TelegramGroupDaoImpl() {
        super(_TelegramGroups.class);
    }

    @Override
    public _TelegramGroups getByChatID(long chatID) {
        return (_TelegramGroups) findSingle("select t from _TelegramGroups t where t.chatId = :chatId", preparing(new Entry("chatId", chatID)));
    }

    @Override
    public _TelegramGroups getByDepartment(long departmentId) {
        return (_TelegramGroups) findSingle("select t from _TelegramGroups t where t.departmentId = :departmentId", preparing(new Entry("departmentId", departmentId)));
    }

    @Transactional
    @Override
    public List<_TelegramGroups> getAll() {
        return (List<_TelegramGroups>) find("select t from _TelegramGroups t where t.state <> :state and t.departmentId is not null", preparing(new Entry("state", _State.DELETED))).collect(Collectors.toList());
    }
}
