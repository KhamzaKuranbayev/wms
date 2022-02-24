package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.ColumnSettingsSortRequest;
import uz.uzcard.genesis.dto.api.req.setting.ColumnSettingsRequest;
import uz.uzcard.genesis.dto.api.req.setting.ColumnSettingsUpdateRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._ColumnSettings;
import uz.uzcard.genesis.hibernate.enums.TableType;

import java.util.List;
import java.util.Map;

public interface ColumnSettingsService {
    PageStream<_ColumnSettings> listByTable(TableType table);

    _ColumnSettings save(ColumnSettingsRequest request);

    List<Map<String, String>> findByTable(TableType table);

    List<String> blackList(String table);

    void applyFilter(String simpleName, CoreMap map);

    void delete(Long id);

    _ColumnSettings update(ColumnSettingsUpdateRequest request);

    _ColumnSettings updateCurrent(ColumnSettingsUpdateRequest request);

    void sort(ColumnSettingsSortRequest request);
}
