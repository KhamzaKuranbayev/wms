package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._PackageType;

public interface PackageTypeDao extends Dao<_PackageType> {
    boolean checkByName(String name);
}
