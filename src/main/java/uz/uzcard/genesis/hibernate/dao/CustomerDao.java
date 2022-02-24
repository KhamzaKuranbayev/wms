package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Customer;

public interface CustomerDao extends Dao<_Customer> {

    _Customer checkCustomerByName(String name);

    _Customer getById(Long id);

    _Customer checkCustomerWithOutName(String name, Long id);
}