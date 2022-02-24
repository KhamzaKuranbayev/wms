package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Role;

import java.util.Set;
import java.util.stream.Stream;

public interface RoleDao extends Dao<_Role> {
    _Role getByCode(String code);

    Stream<_Role> findByIds(Set<Long> ids);
}