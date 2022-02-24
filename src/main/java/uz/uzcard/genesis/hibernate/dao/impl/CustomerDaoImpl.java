package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.CustomerDao;
import uz.uzcard.genesis.hibernate.entity._Customer;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.Map;
import java.util.stream.Stream;

@Component(value = "customerDao")
public class CustomerDaoImpl extends DaoImpl<_Customer> implements CustomerDao {

    public CustomerDaoImpl() {
        super(_Customer.class);
    }

    @Override
    public Stream<_Customer> list(FilterParameters filter) {
        CustomerDaoImpl.CustomFilter customFilter = new CustomerDaoImpl.CustomFilter(filter).invoke();
        String filterQuery = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return findInterval("select distinct t from _Customer t " +
                " where t.state <> 'DELETED' " + filterQuery +
                " order by t.id", params, filter.getStart(), filter.getSize());
    }

    @Override
    public _Customer checkCustomerByName(String name) {
        return (_Customer) findSingle("select t from _Customer t where t.state <> :deleted and lower(trim(t.name)) = lower(trim(:name)) ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("name", name)));
    }

    @Override
    public _Customer getById(Long id) {
        return (_Customer) findSingle("select t from _Customer t where t.state <> :deleted and t.id = :id ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("id", id)));
    }

    @Override
    public _Customer checkCustomerWithOutName(String name, Long id) {
        return (_Customer) findSingle("select t from _Customer t where t.state <> :deleted and lower(trim(t.name)) = lower(trim(:name)) and t.id != :id ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("name", name), new Entry("id", id)));
    }

    private class CustomFilter {
        private final FilterParameters filter;
        private String filterQuery;
        private Map<String, Object> params;

        public CustomFilter(FilterParameters filter) {
            this.filter = filter;
        }

        public String getFilterQuery() {
            return filterQuery;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public CustomFilter invoke() {
            filterQuery = "";
            params = preparing();
            if (filter.has("id")) {
                filterQuery += " and t.id in (:id)";
                params.put("id", filter.getLong("id"));
            }
            if (filter.has("name")) {
                filterQuery += " and t.name = :name";
                params.put("name", filter.getLong("name"));
            }
            return this;
        }
    }
}
