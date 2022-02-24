package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.facet.Facet;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.OrderItemDao;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uz.uzcard.genesis.uitls.StateConstants.DELETED;
import static uz.uzcard.genesis.uitls.StateConstants.NEW;

@Component(value = "orderItemDao")
public class OrderItemDaoImpl extends DaoImpl<_OrderItem> implements OrderItemDao {
    public OrderItemDaoImpl() {
        super(_OrderItem.class);
    }

    @Override
    public List<String> getCategoriesByProduct(Long product_id) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("product.id", "" + product_id);
        List<Facet> facets = getFacets(searchFilter.toString(), "unitType", 10);
        return facets.stream().map(facet -> facet.getValue()).collect(Collectors.toList());
    }

    @Override
    public Stream<_OrderItem> findByIds(List<Long> ids) {
        return find("select t from _OrderItem t where t.id in (:ids)",
                preparing(new Entry("ids", ids)));
    }

    @Override
    public Stream<_OrderItem> findByOrder(_Order order) {
        return find("select t from _OrderItem t where t.state != :deleted and t.parent = :order",
                preparing(new Entry("deleted", _State.DELETED), new Entry("order", order)));
    }

    @Override
    public List findByContractItem(_ContractItem contractItem) {
        return find("select t from _OrderItem t where t.state != :deleted and t.contractItem = :contractItem",
                preparing(new Entry("deleted", _State.DELETED), new Entry("contractItem", contractItem)), Constants.Cache.QUERY_ORDER);
    }

    @Override
    public List findByContractItemAndByNotStates(_ContractItem contractItem, String... state) {
        return find("select t from _OrderItem t where t.state not in ( :states ) and t.contractItem = :contractItem",
                preparing(new Entry("states", Arrays.asList(state)), new Entry("contractItem", contractItem)), Constants.Cache.QUERY_ORDER);
    }

    @Override
    public Boolean findOrderItemsByOrder(_Order order, String... state) {
        return ((Long) findSingle("select count(t) from _OrderItem t where t.parent = :order and t.state not in ( :state )",
                preparing(new Entry("order", order), new Entry("state", Arrays.asList(state))))).intValue() > 0;
    }

    @Override
    public Integer findOrderItemMaxNumb(Long orderId) {
        return (Integer) findSingle("select max(t.itemNumb) from _OrderItem t where t.parent.id = :orderId and t.state <> :deleted",
                preparing(new Entry("orderId", orderId), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public _OrderItem getByProductInOrder(Long orderId, Long productId) {
        return (_OrderItem) findSingle("select t from _OrderItem t where t.state <> :deleted and t.parent.id = :orderId and t.product.id = :productId",
                preparing(new Entry("deleted", _State.DELETED), new Entry("orderId", orderId), new Entry("productId", productId)));
    }

    @Override
    public Double getAllBronByProduct(_Product product) {
        return (Double) findSingle("select sum(t.bron) from _OrderItem t " +
                        " where t.state != :deleted and t.product = :product ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("product", product)));
    }

    @Override
    public _OrderItem getLastByOrder(_Order order) {
        return (_OrderItem) findSingle("select t from _OrderItem t left join t.parent p " +
                        " where t.state != :deleted and p = :order order by t.id desc ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("order", order)));
    }

    @Override
    public List<Facet> getOrderItemsByProductCategory(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.getFromDate() != null && filter.getToDate() != null) {
            searchFilter.and(String.format("auditInfo.creationDate:[%s TO %s]",
                    DateTools.dateToString(filter.getFromDate(), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(filter.getToDate(), DateTools.Resolution.MILLISECOND)));
        } else if (filter.getFromDate() != null) {
            searchFilter.and(String.format("auditInfo.creationDate:[%s TO %s]",
                    DateTools.dateToString(filter.getFromDate(), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.MILLISECOND)));
        } else if (filter.getToDate() != null) {
            searchFilter.and(String.format("auditInfo.creationDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(filter.getToDate(), DateTools.Resolution.MILLISECOND)));
        }
        String state = String.join(" , ", Arrays.asList(_State.NEW, _State.DELETED));
        searchFilter.and("-state:( " + state + " )");
        searchFilter.and("-productType.state", _State.DELETED);
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        return getFacets(searchFilter.toString(), "productType.nameFacet", 10000);
    }

    @Override
    public List<Facet> getDefineOrderStatusCount(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-parent.state", _State.DELETED);
        searchFilter.and("*:*");
        return getFacets(searchFilter.toString(), "state", 10000);
    }

    @Override
    public List<Facet> departmentOrderItems(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.NEW);
        searchFilter.and("-parent.state", _State.DELETED);
        searchFilter.and("*:*");
        switch (SessionUtils.getInstance().getLanguage()) {
            case "en":
                return getFacets(searchFilter.toString(), "department.nameEn", 10000);
            case "ru":
                return getFacets(searchFilter.toString(), "department.nameRu", 10000);
            case "uz":
                return getFacets(searchFilter.toString(), "department.nameUz", 10000);
            default:
                return getFacets(searchFilter.toString(), "department.nameUz", 10000);
        }
    }

    @Override
    public Stream<_OrderItem> findAll(_Order order) {
        return find("select t from _OrderItem t join t.parent p where p = :order",
                preparing(new Entry("order", order)));
    }

    @Override
    public Stream<_OrderItem> findAllByContractItem(_ContractItem contractItem) {
        return find("select t from _OrderItem t left join t.contractItem ci where ci = :contractItem",
                preparing(new Entry("contractItem", contractItem)));
    }

    @Override
    public int getTotalCount(_Order order) {
        return ((Long) findSingle("select count(t.id) from _OrderItem t join t.parent c where c = :order and t.state != :deleted",
                preparing(new Entry("order", order), new Entry("deleted", DELETED)), Constants.Cache.QUERY_CONTRACT_ITEM)).intValue();
    }

    @Override
    public int getAcceptCount(_Order order) {
        Long count = (Long) findSingle("select count(t.id) from _OrderItem t " +
                        " join t.parent c where c = :order and t.state = :status",
                preparing(new Entry("order", order), new Entry("status", _State.RECEIVED)), Constants.Cache.QUERY_CONTRACT_ITEM);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public Stream<Long> getStatusChangedUsers(String name) {
        SearchFilter searchFilter = new SearchFilter();
        if (!StringUtils.isEmpty(name)) {
            searchFilter.or("statusChangedUser.firstName", name.toLowerCase().trim() + "*");
            searchFilter.or("statusChangedUser.lastName", name.toLowerCase().trim() + "*");
            searchFilter.or("statusChangedUser.middleName", name.toLowerCase().trim() + "*");
            searchFilter.or("statusChangedUser.phone", name.toLowerCase().trim() + "*");
            searchFilter.or("statusChangedUser.email", name.toLowerCase().trim() + "*");
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        return getFacets("*:*", "id2", 100).stream().map(facet -> Long.parseLong(facet.getValue()));
    }

    @Override
    public PageStream<_OrderItem> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("order_id")) {
            searchFilter.and("parent.id", filter.getString("order_id"));
        }
        if (filter.getBool("fromOzl")) {
            String state = String.join(" , ", Arrays.asList(_State.PAPER_EXPECTED_SPECIFICATION, _State.PENDING_PURCHASE,
                    _State.APPLICATION_REJECTED_OZL, _State.DELIVERY_EXPECTED, _State.ISSUED_ORDER_ITEM, _State.READY_TO_PRODUCE, _State.RECEIVED));
            SearchFilter temp = new SearchFilter();
            temp.and("state:( " + state + " )");
            temp.or("( state:" + _State.YES_PRODUCT + " AND -contractItem:null )");
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (filter.has("states")) {
            searchFilter.and(String.format("state : ( %s )", filter.getString("states")));
        }
        if (filter.has("orderNumber")) {
            searchFilter.and("parent.numb", filter.getString("orderNumber") + "*");
        }
        if (filter.has("groupId")) {
            searchFilter.and("productGroup.id", filter.getString("groupId"));
        }
        if (filter.has("typeId")) {
            searchFilter.and("productType.id", filter.getString("typeId"));
        }
        if (filter.has("contractId")) {
            searchFilter.and("contractItem.parent.id", filter.getString("contractId"));
        }
        if (filter.has("departmentId")) {
            searchFilter.and("department.id", filter.getString("departmentId"));
        }
        if (filter.has("productName")) {
            searchFilter.and("product.name", filter.getString("productName").trim() + "*");
        }
        if (filter.has("positionState")) {
            searchFilter.and("state", filter.getString("positionState"));
        }
        if (filter.has("requestState")) {
            searchFilter.and("parent.state", filter.getString("requestState"));
        }
        if (filter.has("numbSearch")) {
            searchFilter.and("parent.numb", filter.getString("numbSearch").trim() + "*");
        }
        if (filter.has("contractNumber")) {
            searchFilter.and("contractItem.parent.code", filter.getString("contractNumber").trim() + "*");
        }
        if (filter.has("isHasContract")) {
            searchFilter.and("-contractItem:null");
        }
        if (filter.getBool("forProducing")) {
            searchFilter.and("remains:{0.0 TO *]");
        }
        if (filter.has("supplierId")) {
            searchFilter.and("contractItem.parent.supplier.id", filter.getString("supplierId"));
        }

        if (filter.has("fromDate") && filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("parent.sendDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromDate")) {
            searchFilter.and(String.format("parent.sendDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("parent.sendDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }
        if (filter.has("statusChangedUser")) {
            searchFilter.and("statusChangedUser.id", filter.getString("statusChangedUser"));
        }
        if (filter.has("allSearch")) {
            searchFilter.and("(product.name", filter.getString("allSearch").trim() + "*");
            searchFilter.or("parent.numb", filter.getString("allSearch").trim() + "*)");
        }
        if (filter.getBool("hasGiven")) {
            searchFilter.and("givens.id:[* TO *]");
            searchFilter.and("-givens.state", _State.PRODUCT_ACCEPTED);
            searchFilter.and("department.id", SessionUtils.getInstance().getUser().getDepartment().getId().toString());
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.CONTRACT_REJECT);
        searchFilter.and("-parent.state", _State.DELETED);
//        SearchFilter temp = new SearchFilter();
//        temp.and("department.id", "" + getUser().getDepartment().getId());
//        temp.or("department.parent.id", "" + getUser().getDepartment().getId());
//        searchFilter.and(String.format("( %s )", temp.toString()));
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _OrderItem.class);
        fullTextQuery.setSort(new Sort(new SortField("itemNumbSort", SortField.Type.INT), new SortField("auditInfo.creationDate", SortField.Type.STRING)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_OrderItem>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _OrderItem save(_OrderItem entity) {
        calculate(entity);
        return super.save(entity);
    }

    private void calculate(_OrderItem entity) {
        if (entity.isNew() && entity.getItemNumb() < 1) {
            int maxNumber = getMaxNumberByParent(entity);
            entity.setItemNumb(maxNumber + 1);
        }
        if (!entity.getState().equals(_State.RECEIVED) && entity.getCount() - entity.getGiven() == 0) {
            entity.setState(_State.READY_TO_PRODUCE);
        }
    }

    private int getMaxNumberByParent(_OrderItem entity) {
        Integer numb = (Integer) findSingle("select max(t.itemNumb) from _OrderItem t where t.parent = :order and t.state <> :deleted",
                preparing(new Entry("order", entity.getParent()), new Entry("deleted", _State.DELETED)));
        if (numb == null) return 0;
        return numb.intValue();
    }
}