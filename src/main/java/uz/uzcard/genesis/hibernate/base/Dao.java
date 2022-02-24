package uz.uzcard.genesis.hibernate.base;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.query.facet.Facet;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.hibernate.entity._User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Virus on 19-Aug-16.
 */
public interface Dao<T extends _Entity> {

    T save(T entity);

    void delete(T entity);

    T get(Long id);

    T get(SelectItem item);

    Stream<T> list();

    Stream find(String query, Map<String, ?> params);

    List find(String query, Map<String, ?> params, String cacheName);

    Stream findInterval(String query, Map<String, ?> params, int offset, int limit);

    List findInterval(String query, Map<String, ?> params, String cacheName, int offset, int limit);

    Stream findNativeInterval(String query, Class clazz, Map<String, ?> params, int offset, int limit);

    Stream findNativeInterval(String query, Map<String, ?> params, int offset, int limit);

    Object findSingle(String query, Map<String, ?> params);

    Object findSingleNative(String query, Map<String, ?> params);

    Object findSingle(String query, Map<String, ?> params, String cacheName);

    Session getSession();

    void updateManyToMany(List<T> list, List<Long> items);

    void deleteOneToMany(List<T> list, ArrayList<Long> items);

    PageStream<T> search(FilterParameters filter);

    Stream<T> list(FilterParameters filter);

    Integer total(FilterParameters filter);

    _User getUser();

    T getDefaultEmpty(Long id);

    void delete(Long id);

    Transaction beginTransaction();

    void commit(Transaction transaction);

    List<Facet> getFacets(String query, String fieldName, int maxFacetCount);

    List<Facet> getFacetWithSorting(String query, String fieldName, int maxFacetCount, String sorting);

    void reindex(List<Long> ids);

    CoreMap getMap(Long id);

    CoreMap getMap(Long id, Callback<T> callback);

    <C, R> R call(C domain, String methodName, int outParamType);

    <C> Object callVoid(String methodName, Session session, int outParamType);
}