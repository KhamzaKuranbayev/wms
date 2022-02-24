package uz.uzcard.genesis.hibernate.base;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.facet.Facet;
import org.hibernate.search.query.facet.FacetSortOrder;
import org.hibernate.search.query.facet.FacetingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.StateDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Virus on 19-Aug-16.
 */
public class DaoImpl<T extends _Entity> implements Dao<T> {

    protected static final String specialKeyCharacters = "+-&&||!(){}[]^\"~*?:/\\";

    private static final Logger log = LogManager.getLogger(DaoImpl.class);
    private final Class<T> clazz;
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private StateDao stateDao;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private Gson gson;

    public DaoImpl(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static String normalaizeKeyword(String keyword, boolean onlyShadowing) {
        StringBuilder searchKey = new StringBuilder();
        if (keyword == null) return null;
        for (char ch : keyword.toCharArray()) {
            if (specialKeyCharacters.contains(String.valueOf(ch))) {
                searchKey.append("\\");
            }
            searchKey.append(ch);
        }

        keyword = searchKey.toString().trim().toLowerCase();
        if (!onlyShadowing) {
            searchKey = new StringBuilder();
            searchKey.append("(").append("\"").append(keyword).append("\"");
            searchKey.append(" OR ").append("*").append(keyword).append("* ");
            searchKey.append(")");
        }
        return searchKey.toString();
    }

    @Override
    public T save(T entity) {
        return save(entity, false);
    }

    public T saveWithTransaction(T entity) {
        return save(entity, true);
    }

    protected T save(T entity, boolean withTransaction) {
        if (entity == null) return null;
        List<Field> fields = _Entity.getFields(clazz);
        Optional<Field> optional = fields.stream().filter(x -> _AuditInfo.class.equals(x.getType())).findFirst();
        if (optional.isPresent()) {
            Field field = optional.get();
            field.setAccessible(true);
            try {
                _AuditInfo auditInfo = (_AuditInfo) field.get(entity);
                if (auditInfo == null) {
                    auditInfo = new _AuditInfo();
                    field.set(entity, auditInfo);
                }
                if (auditInfo.getCreatedByUser() == null) {
                    auditInfo.setCreatedByUser(SessionUtils.getInstance().getUser());
                    auditInfo.setCreationDate(new Date());
                }
                auditInfo.setUpdatedByUser(SessionUtils.getInstance().getUser());
                auditInfo.setUpdatedDate(new Date());

            } catch (IllegalAccessException e) {
                log.error(e);
//               //e.printStackTrace();
                ServerUtils.error(log, e);
                throw new ValidatorException("Серверда хатолик юз берди");
            }
        }
        if (entity.isNew() && entity.getState() == null)
            entity.setState(_State.NEW);
//        else if (entity.getState().ordinal() < State.Updated.ordinal())
//            entity.setState(State.Updated);

        if (withTransaction) {
            Session session = getSession();
            if (session.getTransaction() == null || !session.getTransaction().isActive()) {
                session.beginTransaction();
            }
            session.saveOrUpdate(entity);
            session.getTransaction().commit();
        } else {
            getSession().saveOrUpdate(entity);
        }
        return entity;
    }

    @Override
    public void delete(T entity) {
        if (entity == null) return;
        entity.setState(_State.DELETED);
        save(entity);
    }

    @Override
    public T get(Long id) {
        if (id == null) return null;
        return getSession().get(clazz, id);
    }

    @Override
    public T get(SelectItem item) {
        if (item == null) return null;
        if (item.getId() != null) return get(item.getId());
        if (!StringUtils.isEmpty(item.getValue())) return get(Long.parseLong(item.getValue()));
        return null;
    }

    @Override
    public Stream<T> list() {
        String query = "SELECT t FROM " + clazz.getSimpleName() + " t where t.state != 'DELETED' ";
        try {
            if (clazz.newInstance() instanceof _Item) {
                query += " ORDER BY t.name ";
            }
        } catch (InstantiationException e) {
            log.error(e);
//           //e.printStackTrace();
            ServerUtils.error(log, e);
        } catch (IllegalAccessException e) {
            log.error(e);
//           //e.printStackTrace();
            ServerUtils.error(log, e);
            throw new ValidatorException("Серверда хатолик юз берди");
        }
        return getSession().createQuery(query).stream();
    }

    @Override
    public Stream find(String query, Map<String, ?> params) {
        Query queryObject = getSession().createQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        return queryObject.stream();
    }

    @Override
    public List find(String query, Map<String, ?> params, String cacheName) {
        Query queryObject = getSession().createQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }
        queryObject.setCacheable(true);
        queryObject.setCacheRegion(cacheName);

        return queryObject.list();
    }

    @Override
    public Stream findInterval(String query, Map<String, ?> params, int offset, int limit) {
        Query queryObject = getSession().createQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        if (offset >= 0) {
            queryObject.setFirstResult(offset);
        } else
            queryObject.setFirstResult(0);

        if (limit != 0) {
            queryObject.setMaxResults(limit);
        }

        return queryObject.stream();
    }

    @Override
    public List findInterval(String query, Map<String, ?> params, String cacheName, int offset, int limit) {
        Query queryObject = getSession().createQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        if (offset != 0) {
            queryObject.setFirstResult(offset);
        }

        if (limit != 0) {
            queryObject.setMaxResults(limit);
        }
        queryObject.setCacheable(true);
        queryObject.setCacheRegion(cacheName);

        return queryObject.getResultList();
    }

    @Override
    public Stream findNativeInterval(String query, Class clazz, Map<String, ?> params, int offset, int limit) {
        Query queryObject = getSession().createNativeQuery(query, clazz);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        if (offset != 0) {
            queryObject.setFirstResult(offset);
        }

        if (limit != 0) {
            queryObject.setMaxResults(limit);
        }
        return queryObject.stream();
    }

    @Override
    public Stream findNativeInterval(String query, Map<String, ?> params, int offset, int limit) {
        Query queryObject = getSession().createNativeQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        if (offset != 0) {
            queryObject.setFirstResult(offset);
        }

        if (limit != 0) {
            queryObject.setMaxResults(limit);
        }
        return queryObject.stream();
    }

    @Override
    public Object findSingle(String query, Map<String, ?> params) {
        Query queryObject = getSession().createQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        queryObject.setMaxResults(1);
        List list = queryObject.getResultList();
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public Object findSingleNative(String query, Map<String, ?> params) {
        Query queryObject = getSession().createNativeQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        queryObject.setMaxResults(1);
        List list = queryObject.getResultList();
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public Object findSingle(String query, Map<String, ?> params, String cacheName) {
        Query queryObject = getSession().createQuery(query);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                queryObject.setParameter(entry.getKey(), entry.getValue());
            }
        }

        queryObject.setMaxResults(1);
        queryObject.setCacheable(true);
        queryObject.setCacheRegion(cacheName);

        List list = queryObject.getResultList();
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    private Set<String> getAllFields(final Class<?> type) {
        Set<String> fields = new HashSet<String>();
        for (Field field : type.getDeclaredFields()) {
            fields.add(field.getName());
        }

        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }
        return fields;
    }

    private boolean hasProperty(final String propertyName) {
        Set<String> properties = getAllFields(clazz.getClass());
        return properties.contains(propertyName);
    }

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Map<String, Object> preparing(Entry... params) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> item : params) {
            if (item == null) continue;
            map.put(item.getKey(), item.getValue());
        }
        return map;
    }

    public void updateManyToMany(List<T> list, List<Long> items) {
        if (items == null) return;
        for (Long item : items) {
            Optional<T> query = list.stream().filter(x -> item != null && x.getId().equals(item)).findFirst();
            if (!query.isPresent()) {
                T entity = get(item);
                list.add(entity);
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            T entity = list.get(i);
            Optional<Long> query = items.stream().filter(x -> x.equals(entity.getId())).findFirst();
            if (!query.isPresent()) {
                list.remove(entity);
            }
        }
    }

    @Override
    public void deleteOneToMany(List<T> list, ArrayList<Long> items) {
        List<Long> ids = new ArrayList<Long>();
        items.forEach(item -> {
            if (!(item == null || item == null))
                ids.add(item);
        });
        for (int i = list.size() - 1; i > -1; i--) {
            T entity = list.get(i);
            if (!(entity == null || ids.contains(entity.getId()))) {
                delete(entity);
                list.remove(entity);
            }
        }
    }

    @Override
    public PageStream<T> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, clazz);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<T>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Stream<T> list(FilterParameters filter) {
        String query = "SELECT t FROM " + clazz.getSimpleName() + " t ";
        T temp = null;
        try {
            temp = clazz.newInstance();
        } catch (Exception e) {
            log.error(e, e);
        }
        query += " WHERE state != 'DELETED' ";
        Map<String, Object> preparing = preparing();
        if (temp instanceof _Item && !StringUtils.isEmpty(filter.getSearchKey())) {
            query += (" and " + searchByName("t"));
//            query += " and (lower(t.name) like :key or lower(t.name_ru) like :key or lower(t.name_uzl) like :key)";
            preparing.put("key", filter.getSearchQuery());
        }
        if (filter.has("name")) {
            query += " and lower(t.name) like :name ";
            preparing.put("name", "%" + filter.getString("name").toLowerCase() + "%");
        }
        if (filter.has("parentId")) {
            query += " and t.parentId = :parentId ";
            preparing.put("parentId", filter.getLong("parentId"));
        }

        query += " order by id desc";

        return findInterval(query, preparing, filter.getStart(), filter.getSize());
    }

    @Override
    public Integer total(FilterParameters filter) {
        String query = "SELECT count(t) FROM " + clazz.getSimpleName() + " t ";
        T temp = null;
        try {
            temp = clazz.newInstance();
        } catch (Exception e) {
            log.error(e, e);
        }
        query += " WHERE state != 'DELETED' ";
        Map<String, Object> preparing = preparing();
        if (temp instanceof _Item && !StringUtils.isEmpty(filter.getSearchKey())) {
            query += (" and " + searchByName("t"));
//            query += " and (lower(t.name) like :key or lower(t.name_ru) like :key or lower(t.name_uzl) like :key)";
            preparing.put("key", filter.getSearchQuery());
        }

        if (filter.has("name")) {
            query += " and  lower(t.name) like :name ";
            preparing.put("name", "%" + filter.getString("name").toLowerCase() + "%");
        }
        if (filter.has("parentId")) {
            query += " and t.parentId = :parentId ";
            preparing.put("parentId", filter.getLong("parentId"));
        }

        return ((Long) findSingle(query,
                preparing)).intValue();
    }

    @Override
    public _User getUser() {
        return SessionUtils.getInstance().getUser();
    }

    @Override
    public T getDefaultEmpty(Long id) {
        T t = get(id);
        try {
            return t == null ? clazz.newInstance() : t;
        } catch (InstantiationException e) {
            log.error(e);
//           //e.printStackTrace();
            ServerUtils.error(log, e);
        } catch (IllegalAccessException e) {
            log.error(e);
//           //e.printStackTrace();
            ServerUtils.error(log, e);
            throw new ValidatorException("Серверда хатолик юз берди");
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        T obj = get(id);
        if (obj != null)
            delete(obj);
    }

    @Override
    public Transaction beginTransaction() {
        Transaction transaction = getSession().getTransaction();
        if (transaction.isActive())
            return transaction;
        return getSession().beginTransaction();
    }

    @Override
    public void commit(Transaction transaction) {
        try {
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
    }

//    protected String getNameField() {
//        return getField("name");
//    }

//    protected String getField(String name) {
//        return GlobalizationExtentions.getInstance().getLocalizationField(name);
//    }

    @Override
    public List<Facet> getFacets(String query, String fieldName, int maxFacetCount) {
        SearchFilter searchFilter = new SearchFilter().and(query);
        org.apache.lucene.search.Query luceneQuery = queryParser("facet", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, clazz);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);

        String randomText = UUID.randomUUID().toString();
        QueryBuilder builder = fullTextSession().getSearchFactory()
                .buildQueryBuilder()
                .forEntity(clazz)
                .get();
        FacetingRequest machineStatusFacetingRequest = builder.facet()
                .name(randomText)
                .onField(fieldName)
//                .range()
//                .below(DateTools.dateToString(new Date("2019/10/1"), DateTools.Resolution.DAY))
//                .from(DateTools.dateToString(new Date("2019/10/5"), DateTools.Resolution.DAY))
//                .to(DateTools.dateToString(new Date("2019/10/10"), DateTools.Resolution.DAY))
//                .above(DateTools.dateToString(new Date("2019/10/20"), DateTools.Resolution.DAY)).excludeLimit()
//                .orderedBy(FacetSortOrder.RANGE_DEFINITION_ORDER)
                .discrete()
                .orderedBy(FacetSortOrder.COUNT_DESC)
                .includeZeroCounts(false)
                .maxFacetCount(maxFacetCount)
                .createFacetingRequest();
        fullTextQuery.getFacetManager().enableFaceting(machineStatusFacetingRequest);

        return fullTextQuery.getFacetManager().getFacets(randomText);
    }

    @Override
    public List<Facet> getFacetWithSorting(String query, String fieldName, int maxFacetCount, String sorting) {
        SearchFilter searchFilter = new SearchFilter().and(query);
        org.apache.lucene.search.Query luceneQuery = queryParser("facet", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, clazz);
        fullTextQuery.setSort(new Sort(new SortField(sorting, SortField.Type.STRING, true)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);

        String randomText = UUID.randomUUID().toString();
        QueryBuilder builder = fullTextSession().getSearchFactory()
                .buildQueryBuilder()
                .forEntity(clazz)
                .get();
        FacetingRequest machineStatusFacetingRequest = builder.facet()
                .name(randomText)
                .onField(fieldName)
//                .range()
//                .below(DateTools.dateToString(new Date("2019/10/1"), DateTools.Resolution.DAY))
//                .from(DateTools.dateToString(new Date("2019/10/5"), DateTools.Resolution.DAY))
//                .to(DateTools.dateToString(new Date("2019/10/10"), DateTools.Resolution.DAY))
//                .above(DateTools.dateToString(new Date("2019/10/20"), DateTools.Resolution.DAY)).excludeLimit()
//                .orderedBy(FacetSortOrder.RANGE_DEFINITION_ORDER)
                .discrete()
                .orderedBy(FacetSortOrder.FIELD_VALUE)
                .includeZeroCounts(false)
                .maxFacetCount(maxFacetCount)
                .createFacetingRequest();
        fullTextQuery.getFacetManager().enableFaceting(machineStatusFacetingRequest);

        return fullTextQuery.getFacetManager().getFacets(randomText);
    }

    @Override
    public void reindex(List<Long> ids) {
        Stream<T> stream = find("select t from " + clazz.getSimpleName() + " t where t.id in (:ids)",
                preparing(new Entry("ids", ids)));
        stream.forEach(t -> {
            fullTextSession().index(t);
        });
    }

    @Override
    public CoreMap getMap(Long id) {
        T entity = get(id);
        if (entity == null) return null;
        return entity.getMap(true);
    }

    @Override
    public CoreMap getMap(Long id, Callback<T> callback) {
        T entity = get(id);
        if (entity == null) return null;
        return callback.execute(entity, entity.getMap(true));
    }

    protected String searchByName(String tableName) {
        return "(lower(" + tableName + ".name) like :key or lower(" +
                tableName + ".name_ru) like :key or lower(" + tableName +
                ".name_uzl) like :key)";
    }

    protected FullTextSession fullTextSession() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        return fullTextSession;
    }

    protected SearchFactory searchFactory() {
        SearchFactory searchFactory = fullTextSession().getSearchFactory();
        return searchFactory;
    }

    protected QueryParser queryParser(String field) {
        QueryParser parser = new QueryParser(field, searchFactory().getAnalyzer(clazz));
        parser.setAllowLeadingWildcard(true);
        parser.setLowercaseExpandedTerms(true);
        return parser;
    }

    protected org.apache.lucene.search.Query queryParser(String field, String query) {
        return queryParser(field, query, false);
    }

    protected org.apache.lucene.search.Query queryParser(String field, String query, boolean allowLeadingWildcard) {
        QueryParser parser = new QueryParser(field, searchFactory().getAnalyzer(clazz));
        parser.setAllowLeadingWildcard(allowLeadingWildcard);
        parser.setLowercaseExpandedTerms(true);
        parser.setPhraseSlop(3);
        try {
            return parser.parse(query);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected List<String> stringCollection(List<String> list) {
        return list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> String.format("\"%s\"", s)).collect(Collectors.toList());
    }

    @Override
    public <C, R> R call(C domain, String methodName, int outParamType) {
        Session session = entityManager.unwrap(Session.class);

        return (R) call(domain, methodName, session, outParamType);
    }

    public <C> Object call(C domain, String methodName, Session session, int outParamType) {
        return session.doReturningWork(
                connection -> {
                    try (CallableStatement function = connection
                            .prepareCall(
                                    "{ ? = call " + methodName + " (?, ?) }")) {
                        function.registerOutParameter(1, outParamType);
                        function.setString(2, gson.toJson(domain));
                        prepareFunction(function);
                        return returnByOutType(outParamType, function);
                    } catch (Exception ex) {
                        throw new SQLException(ex.getMessage(), ex.getCause());
                    }
                });
    }

    @Override
    public <C> Object callVoid(String methodName, Session session, int outParamType) {
        return session.doReturningWork(
                connection -> {
                    try (CallableStatement function = connection
                            .prepareCall(
                                    "{ ? = call " + methodName + " }")) {
                        function.registerOutParameter(1, outParamType);
                        function.execute();
                        return returnByOutType(outParamType, function);
                    } catch (Exception ex) {
                        throw new SQLException(ex.getMessage(), ex.getCause());
                    }
                });
    }

    private void prepareFunction(CallableStatement function) throws SQLException {
        function.setLong(3, SessionUtils.getInstance().getUser() == null ? -1 : SessionUtils.getInstance().getUser().getId());
        function.execute();

        if (!ServerUtils.isEmpty(function.getWarnings())) {
            throw new RuntimeException(function.getWarnings().getMessage());
        }
    }

    private Serializable returnByOutType(int outParamType, CallableStatement function) throws SQLException {
        switch (outParamType) {
            case Types.BOOLEAN:
                return function.getBoolean(1);
            case Types.VARCHAR:
                return function.getString(1);
            case Types.BIGINT:
                return function.getLong(1);
            case Types.INTEGER:
                return function.getInt(1);
            case Types.NUMERIC:
                return function.getDouble(1);
        }
        return function.getLong(1);
    }

    protected static class Entry implements Map.Entry {
        private final String key;
        private Object value;

        public Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            return this.value = value;
        }
    }

    public class SearchFilter {
        private final StringBuilder query = new StringBuilder();

        public SearchFilter and(String condition) {
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append(condition);
            return this;
        }

        public SearchFilter and(String field, String value) {
            value = value.trim();
            if (query.length() > 0) {
                query.append(" AND ");
            }
            if (value.contains(" ")) {
                value = String.format("(%s)", value);
            }
            if (value.contains("*") && !value.startsWith("-"))
                query.append(field).append(":").append(value);
            else
                query.append(field).append(":\"").append(value).append("\"");
            return this;
        }

        public SearchFilter or(String condition) {
            if (query.length() > 0) {
                query.append(" OR ");
            }
            query.append(condition);
            return this;
        }

        public SearchFilter or(String field, String value) {
            value = value.trim();
            if (query.length() > 0) {
                query.append(" OR ");
            }
            if (value.contains(" ")) {
                value = String.format("(%s)", value);
            }
            if (value.contains("*") && !value.startsWith("-"))
                query.append(field).append(":").append(value);
            else
                query.append(field).append(":\"").append(value).append("\"");
            return this;
        }

        @Override
        public String toString() {
            return query.toString();
        }
    }
}