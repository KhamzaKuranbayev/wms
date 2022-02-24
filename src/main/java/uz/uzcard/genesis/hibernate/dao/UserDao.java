package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.util.List;
import java.util.stream.Stream;

public interface UserDao extends Dao<_User> {
    _User getByUseName(String userName);

    _User getByPhoneNumber(String phoneNumber);

    _User getByEmail(String email);

    Stream<_User> findByIds(List<Long> ids);

    _User getByUseNameWithOutId(String userName, Long id);

    Stream<_User> findByDepartment(_Department department);

    Stream<_User> findByDepartmentType(OrderClassification depType);

    Stream<_User> findByRole(String code);

    Stream<_User> findAllWithoutId(Long withOutId);
}