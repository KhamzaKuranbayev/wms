package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Patient;

public interface PatientDao extends Dao<_Patient> {

    _Patient getByPassportNumber(String passportNumber);
}
