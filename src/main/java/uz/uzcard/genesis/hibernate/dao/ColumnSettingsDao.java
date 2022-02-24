package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._ColumnSettings;
import uz.uzcard.genesis.hibernate.enums.TableType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ColumnSettingsDao extends Dao<_ColumnSettings> {
    List<Map<String, String>> findByTable(TableType table);

    List<String> blackList(TableType table);

    _ColumnSettings getByColumnMyCustom(String columnName, TableType table);

    Stream<_ColumnSettings> findByColumnAllCustom(String columnName, TableType table);

    _ColumnSettings getByColumnDefault(String columnName, TableType table);
}