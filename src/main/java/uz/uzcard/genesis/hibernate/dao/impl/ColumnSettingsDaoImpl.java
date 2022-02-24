package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ColumnSettingsDao;
import uz.uzcard.genesis.hibernate.entity._ColumnSettings;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.enums.TableType;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(value = "columnSettingsDao")
public class ColumnSettingsDaoImpl extends DaoImpl<_ColumnSettings> implements ColumnSettingsDao {
    public ColumnSettingsDaoImpl() {
        super(_ColumnSettings.class);
    }

    @Override
    public PageStream<_ColumnSettings> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("tableType", filter.getString("tableType"));
        if (filter.getBool("isDefault")) {
            searchFilter.and("custom", "false");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ColumnSettings.class);
        fullTextQuery.setSort(new Sort(new SortField("position", SortField.Type.INT)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_ColumnSettings>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public List<Map<String, String>> findByTable(TableType table) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and(String.format("tableType:%s AND -state:%s AND (user.id:%s^9000 OR (roles.code:(%s)^6000 AND custom:false)) AND enable:true",
                table.name(), _State.DELETED, getUser().getId(), String.join(", ", stringCollection(SessionUtils.getInstance().getRoles()))));
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ColumnSettings.class);
        fullTextQuery.setSort(new Sort(new SortField("position", SortField.Type.INT)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        Stream<_ColumnSettings> stream = fullTextQuery.stream();
        Map<String, Map<String, String>> map = new HashMap<>();
        stream.forEach(settings -> {
            if (settings.getUser() != null || !map.containsKey(settings.getColumnName())) {
                CoreMap coreMap = settings.getMap();
                coreMap.remove("id");
                coreMap.remove("state");
                map.put(settings.getColumnName(), coreMap.getInstance());
            }
        });
        return map.values().stream().sorted((o1, o2) -> Integer.parseInt(o1.get("position")) > Integer.parseInt(o2.get("position")) ? 1 : -1).collect(Collectors.toList());
    }

    @Override
    public List<String> blackList(TableType table) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and(String.format("tableType:%s AND -state:%s AND enable:false AND (user.id:%s^9000 OR (roles.code:(%s)^6000 AND custom:false^3000))",
                table.name(), _State.DELETED, getUser().getId(), String.join(", ", stringCollection(SessionUtils.getInstance().getRoles()))));
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ColumnSettings.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        Stream<_ColumnSettings> stream = fullTextQuery.stream();
        return stream.map(columnSettings -> columnSettings.getColumnName()).distinct().collect(Collectors.toList());
    }

    @Override
    public _ColumnSettings getByColumnMyCustom(String columnName, TableType table) {
        return (_ColumnSettings) findSingle("select t from _ColumnSettings t where t.custom = true and t.columnName = :name and t.tableType = :table and t.user = :user ",
                preparing(new Entry("name", columnName), new Entry("table", table), new Entry("user", getUser())));
    }

    @Override
    public Stream<_ColumnSettings> findByColumnAllCustom(String columnName, TableType table) {
        return find("select t from _ColumnSettings t where t.custom = true and t.columnName = :name and t.tableType = :table ",
                preparing(new Entry("name", columnName), new Entry("table", table)));
    }

    @Override
    public _ColumnSettings getByColumnDefault(String columnName, TableType table) {
        return (_ColumnSettings) findSingle("select t from _ColumnSettings t where t.state != :deleted and t.custom = false and t.columnName = :name and t.tableType = :table",
                preparing(new Entry("deleted", _State.DELETED), new Entry("name", columnName), new Entry("table", table)));
    }
}