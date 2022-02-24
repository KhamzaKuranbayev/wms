package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._TelegramGroups;

import java.util.List;

/**
 * @author Javohir Elmurodov
 * @created 1/14/2021 | 10:48 AM
 * @project GTL
 */
public interface TelegramGroupDao extends Dao<_TelegramGroups> {
    _TelegramGroups getByChatID(long chatID);
    _TelegramGroups getByDepartment(long departmentId);
    List<_TelegramGroups> getAll();
}
