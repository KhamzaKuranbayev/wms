package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.PermissionDao;
import uz.uzcard.genesis.hibernate.entity._Permission;

@Component(value = "permissionDao")
public class PermissionDaoImpl extends DaoImpl<_Permission> implements PermissionDao {
    public PermissionDaoImpl() {
        super(_Permission.class);
    }

    @Override
    public _Permission getByCode(String code) {
        return (_Permission) findSingle("select t from _Permission t where t.code = :code",
                preparing(new Entry("code", code)));
    }
}
