package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductDao;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._User;

@Component(value = "productDao")
public class ProductDaoImpl extends DaoImpl<_Product> implements ProductDao {
    public ProductDaoImpl() {
        super(_Product.class);
    }

    @Override
    public PageStream<_Product> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("productId"))
            searchFilter.and("id", filter.getString("productId"));
        if (filter.has("name") && filter.getString("name").contains(",")) {
            String name = filter.getString("name");
            searchFilter.and("name", name.split(",")[0] + "*");
            searchFilter.and(String.format("attr : ( *%s* )", QueryParser.escape(name.substring(name.indexOf(",") + 1).trim()).replaceAll("  ", " ").replaceAll(" ", "* AND *")));
        } else if (filter.has("name")) {
            if (filter.getString("name").contains("*") || filter.getString("name").contains("?")) {
                searchFilter.and("name", filter.getString("name").trim());
            } else {
                searchFilter.and("name", QueryParser.escape(filter.getString("name")).trim() + "*");
            }
        }
        if (filter.has("group_id"))
            searchFilter.and("group.id", filter.getString("group_id"));
        if (filter.has("type_id"))
            searchFilter.and("type.id", filter.getString("type_id"));
        if (filter.getBool("materialesThatEnd"))
            searchFilter.and("limit:{0.0 TO *]");
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Product.class);
        if ("remains_limit_count_diff".equals(filter.getSortColumn()))
            fullTextQuery.setSort(new Sort(new SortField("remains_limit_count_diff", SortField.Type.DOUBLE, false)));
        else
            fullTextQuery.setSort(new Sort(new SortField("nameSort", SortField.Type.STRING)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Product>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Product save(_Product entity) {
        if (entity.getLimitCount() != null && entity.getLimitCount() > 0) {
            Double percentage = 100 * entity.getCount() / entity.getLimitCount();
            entity.setPercentRemainingToLimit(percentage.intValue());
        } else entity.setPercentRemainingToLimit(0);
        super.save(entity);
        return entity;
    }

//    @Override
//    public Stream<_Product> list(FilterParameters filter) {
//        CustomFilter customFilter = new CustomFilter(filter).invoke();
//        String filterQuery = customFilter.getFilterQuery();
//        Map<String, Object> params = customFilter.getParams();
//        return findInterval("select distinct t from _Product t left join t.type tp left join t.group gr left join t.attributes attribute " +
//                " where t.state != 'DELETED' " + filterQuery, params, filter.getStart(), filter.getSize());
//    }

//    @Override
//    public Integer total(FilterParameters filter) {
//        CustomFilter customFilter = new CustomFilter(filter).invoke();
//        String filterQuery = customFilter.getFilterQuery();
//        Map<String, Object> params = customFilter.getParams();
//        return ((Long) findSingle("select distinct count(t) from _Product t left join t.type tp left join t.group gr left join t.attributes attribute " +
//                " where t.state != 'DELETED' " + filterQuery, params)).intValue();
//    }

    @Override
    public boolean checkByName(String name) {
        return (Long) findSingle("select count(t) from _Product t where lower(trim(t.name)) = lower(trim(:name)) and t.state <> :deleted ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("name", name))) > 0;
    }

    @Override
    public _Product getByUniqueKey(String uniqueKey) {
        return (_Product) findSingle("select t from _Product t where t.state <> :deleted and t.uniqueKey = :uniqueKey ",
                preparing(new Entry("deleted", _State.DELETED),
                        new Entry("uniqueKey", uniqueKey)));
    }

    @Override
    public _Product getByUniqueKeyWithoutId(String uniqueKey, Long id) {
        return (_Product) findSingle("select t from _Product t where t.state <> :deleted and t.uniqueKey = :uniqueKey and t.id != :id ",
                preparing(new Entry("deleted", _State.DELETED),
                        new Entry("uniqueKey", uniqueKey),
                        new Entry("id", id)));
    }

//    private class CustomFilter {
//        private FilterParameters filter;
//        private String filterQuery;
//        private Map<String, Object> params;
//
//        public CustomFilter(FilterParameters filter) {
//            this.filter = filter;
//        }
//
//        public String getFilterQuery() {
//            return filterQuery;
//        }
//
//        public Map<String, Object> getParams() {
//            return params;
//        }
//
//        public CustomFilter invoke() {
//            filterQuery = "";
//            params = preparing();
//            if (filter.has("name")) {
//                filterQuery += " and lower(t.name) like lower(:name) ";
//                params.put("name", filter.getString("name"));
//            }
//            if (filter.has("groupId")) {
//                filterQuery += " and gr.id = :groupId ";
//                params.put("groupId", filter.getLong("groupId"));
//            }
//            if (filter.has("typeId")) {
//                filterQuery += " and tp.id = :typeId ";
//                params.put("typeId", filter.getLong("typeId"));
//            }
//            if (filter.has("withAttribute")) {
//                filterQuery += " and attribute is not null ";
//            }
//            if (filter.has("withOutAttribute")) {
//                filterQuery += " and attribute is null ";
//            }
//            return this;
//        }
//    }
}