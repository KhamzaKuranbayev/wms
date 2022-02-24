package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ContractDao;
import uz.uzcard.genesis.hibernate.dao.ContractItemDao;
import uz.uzcard.genesis.hibernate.entity.*;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

import static uz.uzcard.genesis.uitls.StateConstants.*;

@Component(value = "contractItemDao")
public class ContractItemDaoImpl extends DaoImpl<_ContractItem> implements ContractItemDao {
    @Autowired
    private ContractDao contractDao;

    public ContractItemDaoImpl() {
        super(_ContractItem.class);
    }

    @Override
    public PageStream<_ContractItem> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("contractId"))
            searchFilter.and("parent.id", filter.getString("contractId"));
        else if (filter.has("contractCode")) {
            SearchFilter temp = new SearchFilter();
            if (filter.getString("contractCode").equals(QueryParser.escape(filter.getString("contractCode")))) {
                temp.and("parent.code", filter.getString("contractCode"));
                temp.or("partitions.givens.orderItem.contractItem.parent.code", filter.getString("contractCode") + "*");
            } else {
                temp.and("parent.code", QueryParser.escape(filter.getString("contractCode")));
                temp.or("partitions.givens.orderItem.contractItem.parent.code", QueryParser.escape(filter.getString("contractCode")));
            }
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (filter.has("groupId"))
            searchFilter.and("productGroup.id", filter.getString("groupId"));
        if (filter.has("typeId"))
            searchFilter.and("productType.id", filter.getString("typeId"));
        if (filter.has("productName"))
            searchFilter.and("product.name", filter.getString("productName").trim() + "*");
        if (filter.has("contractStatus"))
            searchFilter.and("parent.state", filter.getString("contractStatus"));
        if (filter.has("state"))
            searchFilter.and("state", filter.getString("state"));

        if (filter.has("fromDate") && filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("parent.conclusionDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromDate")) {
            searchFilter.and(String.format("parent.conclusionDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("parent.conclusionDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }
        if (filter.getBool("forPrintQrCode")) {
            searchFilter.and(String.format("state : ( %s )",
                    String.join(", ", stringCollection(Arrays.asList(CONTRACT_ITEM_ACCEPTED, CONTRACT_ITEM_PART_ACCEPTED, CONTRACT_ITEM_PARTITION_ACCEPTED)))));
            searchFilter.and("-partitions.lots:null");
            if (filter.has("forQrCode")) {
                searchFilter.and("partitions.lots.qrPrinted", filter.getString("forQrCode"));
            }
        }
        if (filter.has("orderNumber")) {
//            searchFilter.and("orderItems.parent.numb", filter.getString("orderNumber"));
            searchFilter.and("partitions.givens.orderItem.parent.numb", filter.getString("orderNumber") + "*");
        }
        if (filter.has("productId")) {
            searchFilter.and("product.id", filter.getString("productId"));
        }

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        if (filter.has("state"))
            searchFilter.and("state", filter.getString("state"));

        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-parent.state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ContractItem.class);
        if (filter.getBool("forPrintQrCode")) {
            fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, true)));
        } else
            fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, false)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_ContractItem>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _ContractItem save(_ContractItem entity) {
        super.save(entity);
        calculate(entity);
        return entity;
    }

    private void calculate(_ContractItem entity) {
        if (entity.getParent() != null) {
            _Contract contract = entity.getParent();
            contract.setAcceptCount(getAcceptCount(contract));
            contract.setTotalCount(getTotalCount(contract));
            contractDao.save(contract);
        }
    }


    @Override
    public int getAcceptCount(_Contract contract) {
        Long count = (Long) findSingle("select sum(case t.initiatorCount when t.initiatorAcceptedCount then 1 else 0 end) from _ContractItem t " +
                        " join t.parent c where c = :contract and t.state not in (:states)",
                preparing(new Entry("contract", contract), new Entry("states", Arrays.asList(NEW, DELETED))), Constants.Cache.QUERY_CONTRACT_ITEM);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public int getMaxNumb(_Contract contract) {
        return ((Long) findSingle("select case when max(t.numb) = null then 0L else max(t.numb) end " +
                        " from _ContractItem t join t.parent c where c = :contract and t.state <> :accept",
                preparing(new Entry("contract", contract), new Entry("accept", DELETED)))).intValue();
    }

    @Override
    public Stream<_ContractItem> getByContract(_Contract contract) {
        return ((Stream<_ContractItem>) find("select t from _ContractItem t join t.parent c where c = :contract and t.state = :accept",
                preparing(new Entry("contract", contract), new Entry("accept", CONTRACT_ITEM_ACCEPTED))));
    }

    @Override
    public Stream<_ContractItem> findByParent(_Contract contract) {
        return ((Stream<_ContractItem>) find("select t from _ContractItem t join t.parent c where c = :contract and t.state <> :state",
                preparing(new Entry("contract", contract), new Entry("state", DELETED))));
    }

    @Override
    public int getTotalCount(_Contract contract) {
//        return ((BigInteger) findSingleNative("select count(1) from contract_item t where t.parent_id = :contractId and t.state != :deleted",
//                preparing(new Entry("contractId", contract.getId()), new Entry("deleted", DELETED)))).intValue();
        return ((Long) findSingle("select count(t.id) from _ContractItem t join t.parent c where c = :contract and t.state != :deleted",
                preparing(new Entry("contract", contract), new Entry("deleted", DELETED)), Constants.Cache.QUERY_CONTRACT_ITEM)).intValue();
    }

    @Override
    public _ContractItem getByContractAndProduct(_Contract contract, _Product product, _UnitType unitType) {
        return (_ContractItem) findSingle("select t from _ContractItem t join t.parent c join t.unitType u join t.product p " +
                        " where t.state != :deleted and c = :contract and p = :product and u = :unitType",
                preparing(new Entry("contract", contract), new Entry("product", product), new Entry("unitType", unitType), new Entry("deleted", DELETED)));
    }

    @Override
    public Stream<_ContractItem> findAllByParent(_Contract contract) {
        return find("select t from _ContractItem t join t.parent c where c = :contract",
                preparing(new Entry("contract", contract)));
    }
}