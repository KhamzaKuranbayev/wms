package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.ColumnSettingsSortRequest;
import uz.uzcard.genesis.dto.api.req.setting.ColumnSettingsRequest;
import uz.uzcard.genesis.dto.api.req.setting.ColumnSettingsUpdateRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ColumnSettingsDao;
import uz.uzcard.genesis.hibernate.dao.RoleDao;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.entity._ColumnSettings;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.enums.TableType;
import uz.uzcard.genesis.service.ColumnSettingsService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ColumnSettingsServiceImpl implements ColumnSettingsService {
    @Autowired
    private ColumnSettingsDao columnSettingsDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;

    @Override
    public PageStream<_ColumnSettings> listByTable(TableType table) {
        if (table == null)
            throw new ValidatorException(GlobalizationExtentions.localication("TABLE_NOT_SELECTED"));
        return columnSettingsDao.search(new FilterParameters() {{
            setSize(Integer.MAX_VALUE);
            add("tableType", table.name());
            addBool("isDefault", true);
        }});
    }

    @Override
    public _ColumnSettings save(ColumnSettingsRequest request) {
        if (ServerUtils.isEmpty(request)) {
            throw new ValidatorException("REQUEST_IS_NULL");
        }
        _ColumnSettings columnSettings = columnSettingsDao.get(request.getId());
        if (columnSettings == null) {
            columnSettings = new _ColumnSettings();
            columnSettings.setColumnName(request.getColumnName());
        }
        columnSettings.setColumnLabel(request.getColumnLabel());
        columnSettings.setMinWidth(request.getMinWidth());
        columnSettings.setPosition(request.getPosition());
        columnSettings.setSortable(request.isSortable());
        columnSettings.setTableType(request.getTable());
        columnSettings.setVisible(request.isVisible());
        columnSettings.setEnable(request.isEnable());
        if (request.getRoleIds() == null && !request.isGeneral() && columnSettingsDao.getUser() != null) {
            columnSettings.setUser(columnSettingsDao.getUser());
            columnSettings.setCustom(true);
            columnSettings.getRoles().clear();
        } else {
            columnSettings.setRoles(roleDao.findByIds(request.getRoleIds()).collect(Collectors.toList()));
            _ColumnSettings exist = columnSettingsDao.getByColumnDefault(request.getColumnName(), request.getTable());
            if (!(exist == null || exist.equals(columnSettings))) {
                throw new ValidatorException(GlobalizationExtentions.localication("COLUMN_SETTINGS_NOT_FOUND"));
            }
            _ColumnSettings finalColumnSettings = columnSettings;
            columnSettingsDao.findByColumnAllCustom(columnSettings.getColumnName(), columnSettings.getTableType())
                    .forEach(settings -> {
                        settings.setEnable(finalColumnSettings.isEnable());
                        settings.setMinWidth(finalColumnSettings.getMinWidth());
//                        settings.setColumnName(finalColumnSettings.getColumnName());
                        settings.setColumnLabel(finalColumnSettings.getColumnLabel());
//                        settings.setPosition(finalColumnSettings.getPosition());
                        settings.setSortable(finalColumnSettings.isSortable());
                        settings.getRoles().clear();
                        if (settings.getUser() != null) {
                            settings.setEnable(settings.getUser().getRoles().stream().anyMatch(role -> request.getRoleIds().contains(role.getId())));
                        }
                        columnSettingsDao.save(settings);
                    });
        }
        columnSettingsDao.save(columnSettings);
        return columnSettings;
    }

    @Override
    public List<Map<String, String>> findByTable(TableType table) {
        return columnSettingsDao.findByTable(table);
    }

    @Override
    public List<String> blackList(String table) {
        return columnSettingsDao.blackList(TableType.valueOf(table));
    }

    @Override
    public void applyFilter(String className, CoreMap map) {
        blackList(className).forEach(column -> {
            map.remove(column);
        });
    }

    @Override
    public void delete(Long id) {
        _ColumnSettings columnSettings = columnSettingsDao.get(id);
        if (ServerUtils.isEmpty(columnSettings)) {
            throw new ValidatorException("COLUMN_SETTINGS_IS_NULL");
        }
        if (!columnSettings.isCustom()) {
            columnSettingsDao.findByColumnAllCustom(columnSettings.getColumnName(), columnSettings.getTableType())
                    .forEach(settings -> {
                        columnSettingsDao.delete(settings);
                    });
        }
        columnSettingsDao.delete(id);
    }

    @Override
    public _ColumnSettings update(ColumnSettingsUpdateRequest request) {
        _ColumnSettings columnSettings = columnSettingsDao.getByColumnMyCustom(request.getColumnName(), request.getTable());
        if (columnSettings != null) {
            columnSettings.setState(_State.NEW);
            columnSettings.setEnable(columnSettingsDao.getByColumnDefault(request.getColumnName(), request.getTable()).isEnable());
            columnSettings.getRoles().clear();
        } else {
            columnSettings = copy(request);
        }
        if (request.getVisible() != null)
            columnSettings.setVisible(request.getVisible());
        if (StringUtils.isEmpty(columnSettings.getColumnLabel()))
            throw new ValidatorException("Таблица устуни номини киритинг");
        return columnSettingsDao.save(columnSettings);
    }

    @Override
    public _ColumnSettings updateCurrent(ColumnSettingsUpdateRequest request) {
        _ColumnSettings columnSettings = update(request);
        columnSettings.setMinWidth(request.getWidth());
        columnSettingsDao.save(columnSettings);
        return columnSettings;
    }

    private _ColumnSettings copy(ColumnSettingsUpdateRequest request) {
        _ColumnSettings columnSettings = columnSettingsDao.getByColumnDefault(request.getColumnName(), request.getTable());
        if (columnSettings == null)
            throw new ValidatorException(GlobalizationExtentions.localication("COLUMN_SETTINGS_NOT_FOUND"));
        CoreMap map = columnSettings.getMap();
        _ColumnSettings columnSettingsNew = new _ColumnSettings();
        map.setId(null);
        map.remove("id");
        columnSettingsNew.setMap(map);
        columnSettingsNew.getRoles().clear();
        columnSettingsNew.setCustom(true);
        columnSettingsNew.setTableType(request.getTable());
        columnSettingsNew.setUser(userDao.getUser());
        return columnSettings;
    }

    @Override
    public void sort(ColumnSettingsSortRequest request) {
        List<Map<String, String>> columns = findByTable(request.getTable());
        List<String> cols = columns.stream().map(map -> map.get("columnName")).collect(Collectors.toList());

        int index = cols.indexOf(request.getColumnName());
        if (index < 0)
            throw new ValidatorException("COLUMN_SETTINGS_NOT_FOUND");
        cols.remove(index);
        cols.add(request.getIndex(), request.getColumnName());

        for (String column : cols) {
            _ColumnSettings columnSettings = update(new ColumnSettingsUpdateRequest() {{
                setColumnName(column);
                setTable(request.getTable());
            }});
            columnSettings.setPosition(cols.indexOf(column));
            columnSettingsDao.save(columnSettings);
        }
    }
}