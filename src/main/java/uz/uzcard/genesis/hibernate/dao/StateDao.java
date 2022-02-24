package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.stream.Stream;

public interface StateDao extends Dao<_State> {

    _State getByCode(String code);

    Stream<SelectItem> getItems(String entityName);
}