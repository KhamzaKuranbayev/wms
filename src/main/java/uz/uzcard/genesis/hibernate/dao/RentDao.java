package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._ProductItem;
import uz.uzcard.genesis.hibernate.entity._Rent;

public interface RentDao extends Dao<_Rent> {

    _Rent getByProductIdAndDepartment(_ProductItem productItem, _Department department);
}
