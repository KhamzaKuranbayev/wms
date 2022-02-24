package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Permission;

public interface PermissionDao extends Dao<_Permission> {
    _Permission getByCode(String code);
}