package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.PackageTypeDao;
import uz.uzcard.genesis.hibernate.entity._PackageType;

@Component(value = "packageTypeDao")
public class PackageTypeDaoImpl extends DaoImpl<_PackageType> implements PackageTypeDao {
    public PackageTypeDaoImpl() {
        super(_PackageType.class);
    }

    @Override
    public boolean checkByName(String name) {
        return (Long) findSingle("select count (t) from _PackageType t where lower(trim(t.name))=lower(trim(:name)) ",
                preparing(new Entry("name", name))) > 0;
    }
}