package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.facet.Facet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.WarehouseSearchEngineFilterRequest;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseFilterRequest;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductItemDao;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.PartitionService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(value = "productItemDao")
public class ProductItemDaoImpl extends DaoImpl<_ProductItem> implements ProductItemDao {

    @Autowired
    private PartitionService partitionService;

    public ProductItemDaoImpl() {
        super(_ProductItem.class);
    }

    @Override
    public PageStream<_ProductItem> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("isForPrintQrCode")) {
            searchFilter.and("-partition.contractItem:null AND *:*");
//            searchFilter.and("-state", _State.PRODUCT_PRODUCED);
        }
        if (filter.has("warehouseId")) {
            searchFilter.and("warehouse.id", filter.getString("warehouseId"));
        }
        if (filter.has("warehouseIds"))
            searchFilter.and(String.format("warehouse.id:( %s )", String.join(", ", stringCollection(filter.getStrings("warehouseIds")))));
        if (filter.has("productId"))
            searchFilter.and("product.id", filter.getString("productId"));
        if (filter.has("takenAwayUserId"))
            searchFilter.and("takenAwayUser.id", filter.getString("takenAwayUserId"));
        if (filter.has("takenAwayUserDepartmentId"))
            searchFilter.and("takenAwayUser.department.pr_for_id", filter.getString("takenAwayUserDepartmentId"));
        if (filter.has("contractId"))
            searchFilter.and("partition.contractItem.parent.id", filter.getString("contractId"));
        if (filter.has("state"))
            searchFilter.and("state", filter.getString("state"));
        if (filter.has("orderNumb")) {
            SearchFilter temp = new SearchFilter();
            temp.and("partition.givens.orderItem.parent.numb", filter.getString("orderNumb"));
            temp.or("partition.contractItem.orderItems.parent.numb", filter.getString("orderNumb"));
            temp.or("orderItem.parent.numb", filter.getString("orderNumb"));
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (filter.has("orderItemNumb")) {
            SearchFilter temp = new SearchFilter();
            temp.and("partition.givens.orderItem.itemNumb", filter.getString("orderItemNumb"));
            temp.or("partition.contractItem.orderItems.itemNumb", filter.getString("orderItemNumb"));
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (filter.has("contractNumb")) {
            SearchFilter temp = new SearchFilter();
            temp.and("partition.givens.contractItem.parent.code", filter.getString("contractNumb"));
            temp.or("partition.contractItem.parent.code", filter.getString("contractNumb"));
            temp.or("orderItem.contractItem.parent.code", filter.getString("contractNumb"));
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (filter.has("carriageId")) {
            searchFilter.and("carriages_id", filter.getString("carriageId"));
        }
        if (filter.has("cellId")) {
            searchFilter.and("cells_id", filter.getString("cellId"));
        }
        if (filter.has("name")) {
            SearchFilter temp = new SearchFilter();
            temp.and("name", filter.getString("name") + "*");
            temp.or("product.name", filter.getString("name") + "*");
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (filter.has("lotName")) {
            searchFilter.and("lot.name", filter.getString("lotName"));
        }
        if (filter.has("lotId")) {
            searchFilter.and("lot.id", filter.getString("lotId"));
        }
        if (filter.has("partitionId")) {
            searchFilter.and("partition.id", filter.getString("partitionId"));
        }
        if (filter.has("accountingCode")) {
            searchFilter.and("accountingCode", filter.getString("accountingCode"));
        }
        if (filter.has("placementType")) {
            searchFilter.and("placementType", filter.getString("placementType"));
        }
        if (filter.has("used")) {
            searchFilter.and("used", "" + filter.getBool("used"));
        }
        /*only for PRODUCT_USED status*/
        if (filter.has("fromDate") && filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("auditInfo.updatedDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromDate")) {
            searchFilter.and(String.format("auditInfo.updatedDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("auditInfo.updatedDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.PRODUCT_INVALID);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ProductItem.class);
        if ("idSort".equals(filter.getSortColumn()))
            fullTextQuery.setSort(new Sort(new SortField("idSort", SortField.Type.LONG, true)));
        else if ("takenAwayDate".equals(filter.getSortColumn()))
            fullTextQuery.setSort(new Sort(new SortField("takenAwayDate", SortField.Type.STRING, true)));
        else
            fullTextQuery.setSort(new Sort(new SortField("inventarizationDate", SortField.Type.STRING)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_ProductItem>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public LinkedList<Long> getPackageTypesByProduct(Long product_id) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("product.id", "" + product_id);
        List<Facet> facets = getFacets(searchFilter.toString(), "packages.type.id2", 10);
        LinkedList<Long> items = new LinkedList<>();

        facets.forEach(facet -> {
            long packageTypeId = Long.parseLong(facet.getValue());
            items.add(packageTypeId);
        });
        return items;
    }

    @Override
    public LinkedList<Long> getPackageByProjectAndPackageType(Long product_id, Long packageType_id) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("product.id", "" + product_id);
        searchFilter.and("packages.type.id", "" + packageType_id);
        List<Facet> facets = getFacets(searchFilter.toString(), "packages.id2", 10);
        LinkedList<Long> items = new LinkedList<>();

        facets.forEach(facet -> {
            long packageTypeId = Long.parseLong(facet.getValue());
            items.add(packageTypeId);
        });
        return items;
    }

    @Override
    public Stream<_ProductItem> findAllByPartition(_Partition partition) {
        return find("select t from _ProductItem t where t.partition = :partition",
                preparing(new Entry("partition", partition)));
    }

    @Override
    public Stream<_ProductItem> getForReturning(Set<_Warehouse> warehouses) {
        return find("select t from _ProductItem t where t.state <> :deleted and t.warehouse in (:warehouses) and t.count > 0",
                preparing(new Entry("deleted", _State.DELETED), new Entry("warehouses", warehouses)));
    }

    @Override
    public List<Facet> facetByUserDepartment(DashboardFilter filterRequest) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and(String.format("takenAwayDate:[%s TO %s]",
                DateTools.dateToString(filterRequest.getFromDate(), DateTools.Resolution.MILLISECOND),
                DateTools.dateToString(filterRequest.getToDate(), DateTools.Resolution.MILLISECOND)));

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.PRODUCT_INVALID);
        return getFacets(searchFilter.toString(), "takenAwayUserDepartment", 10000);
    }

    @Override
    public boolean hasAnyByWarehouse(Long warehouseId) {
        Long count = (Long) findSingle("select count(t) from _ProductItem t left join t.warehouse w " +
                        " where t.state not in (:deleted) and w.id = :warehouseId",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_PRODUCED, _State.PRODUCT_INVALID)),
                        new Entry("warehouseId", warehouseId)));
        if (count == null || count.intValue() == 0)
            return false;
        return true;
    }

    @Override
    public Stream<_ProductItem> findAllByLot(_Lot lot) {
        return find("select t from _ProductItem t left join t.lot l where l = :lot",
                preparing(new Entry("lot", lot)));
    }

    @Override
    public Stream<_ProductItem> findByQrCodes(List<Long> ids) {
        return find("select t from _ProductItem t where t.state not in (:states) and t.qrcode in (:ids)",
                preparing(new Entry("states", Arrays.asList(_State.DELETED, _State.PRODUCT_PRODUCED, _State.PRODUCT_INVALID)),
                        new Entry("ids", ids)));
    }

    @Override
    public Stream<_ProductItem> findByIds(List<Long> ids) {
        return find("select t from _ProductItem t where t.state not in (:deleted) and t.id in (:ids)",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID)), new Entry("ids", ids)));
    }

    @Override
    public Stream<_ProductItem> findAllByIds(List<Long> ids) {
        return find("select t from _ProductItem t where t.id in (:ids)", preparing(new Entry("ids", ids)));
    }

    @Override
    public Double findCountByIds(List<Long> ids) {
        return (Double) findSingle("select sum(t.count) from _ProductItem t where t.state not in (:states) and t.id in (:ids)",
                preparing(new Entry("states", Arrays.asList(_State.DELETED, _State.PRODUCT_PRODUCED, _State.PRODUCT_INVALID)),
                        new Entry("ids", ids)));
    }

    @Override
    public Double findCountByPartition(_Partition partition, boolean isAll) {

        Double count;
        if (isAll) {
            count = (Double) findSingle("select sum(t.count) from _ProductItem t where t.state not in (:deleted) and t.partition = :partition",
                    preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID)), new Entry("partition", partition)));
        } else
            count = (Double) findSingle("select sum(t.count) from _ProductItem t where t.state not in (:deleted) and t.partition = :partition",
                    preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID, _State.PRODUCT_PRODUCED)),
                            new Entry("partition", partition)));
        return count;
    }

    @Override
    public Stream<_ProductItem> findByPartition(_Partition partition) {
        return find("select t from _ProductItem t where t.state not in (:deleted) and t.partition = :partition",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID, _State.PRODUCT_PRODUCED)),
                        new Entry("partition", partition)));
    }

    @Override
    public List<Long> findIdsByPartition(_Partition partition) {
        return (List<Long>) find("select t.id from _ProductItem t where t.state not in (:deleted) and t.partition = :partition",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID, _State.PRODUCT_PRODUCED)),
                        new Entry("partition", partition))).collect(Collectors.toList());
    }

    @Override
    public List<Long> findIdsByPartitionId(Long partitionId) {
        return (List<Long>) find("select t.id from _ProductItem t where t.state not in (:deleted) and t.partition.id = :partitionId",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID, _State.PRODUCT_PRODUCED)),
                        new Entry("partitionId", partitionId))).collect(Collectors.toList());
    }

    @Override
    public boolean hasProductItemByCarriage(_Carriage carriage) {
        return (Boolean) findSingleNative("select case when count(1) > 0 then true else false end " +
                        " from product_item where (carriages_id @> '\"" + carriage.getId() + "\"' or carriages_id @> '" + carriage.getId() + "') and state not in (:deleted)",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID))));
    }

    @Override
    public Stream<_ProductItem> findByLot(Long lotId) {
        return find("select t from _ProductItem t left join t.lot l " +
                        " where t.state not in :deleted and l.state not in (:deleted) and l.id = :lotId ",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID)), new Entry("lotId", lotId)));
    }

    @Override
    public double getCountByLot(_Lot lot) {
        Double count = (Double) findSingle("select sum(t.count) from _ProductItem t where t.state not in (:deleted) and t.lot = :lot ",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID)), new Entry("lot", lot)));
        return count == null ? 0 : count;
    }

    @Override
    public double getRemainsByLot(_Lot lot) {
        Double count = (Double) findSingle("select sum(t.count) from _ProductItem t where t.state not in (:deleted) and t.lot = :lot ",
                preparing(new Entry("deleted", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID, _State.PRODUCT_PRODUCED)), new Entry("lot", lot)));
        return count == null ? 0 : count;
    }

    @Override
    public _ProductItem getByAccountingCode(String accountingCode) {
        return (_ProductItem) findSingle("select t from _ProductItem t where t.accountingCode = :accountingCode",
                preparing(new Entry("accountingCode", accountingCode)));
    }

    @Override
    public _ProductItem getByQrCode(Long qrCode) {
        return (_ProductItem) findSingle("select t from _ProductItem t " +
                        " where t.state not in (:states) and t.qrcode = :qrCode",
                preparing(new Entry("states", Arrays.asList(_State.DELETED, _State.PRODUCT_INVALID)), new Entry("qrCode", qrCode)));
    }

    @Override
    public _ProductItem getByQrCodeNewOnly(Long qrCode) {
        return (_ProductItem) findSingle("select t from _ProductItem t " +
                        " where t.state = 'NEW' and t.qrcode = :qrCode",
                preparing(new Entry("qrCode", qrCode)));
    }

    @Override
    public List<Facet> searchWarehouseByProduct(WarehouseSearchEngineFilterRequest request) {
        SearchFilter searchFilter = new SearchFilter();
        if (!StringUtils.isEmpty(request.getName())) {
            SearchFilter temp = new SearchFilter();
            temp.and("name", request.getName().toLowerCase().trim() + "*");
            temp.or("accountingCode", request.getName().toLowerCase().trim());
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.PRODUCT_INVALID);
//        searchFilter.and("valid:true");
        List<Facet> facets = getFacets(searchFilter.toString(), "warehouse.id2", 10000);
        return facets;
    }

    @Override
    public LinkedHashSet<Long> searchWarehouseYByProduct(WarehouseFilterRequest request) {
        SearchFilter searchFilter = new SearchFilter();
        if (!StringUtils.isEmpty(request.getTerm())) {
            SearchFilter temp = new SearchFilter();
            temp.and("name", request.getTerm().toLowerCase().trim() + "*");
            temp.or("accountingCode", request.getTerm().toLowerCase().trim());
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (request.getInventarizationDate() != null) {
            SearchFilter temp = new SearchFilter();
            temp.and(String.format("inventarizationDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.DAY),
                    DateTools.dateToString(request.getInventarizationDate(), DateTools.Resolution.DAY)));
            temp.or("inventarizationLog:null");
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (request.getId() != null)
            searchFilter.and("warehouse.id", "" + request.getId());
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.PRODUCT_INVALID);

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ProductItem.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setSort(new Sort(new SortField("inventarizationDate", SortField.Type.STRING)));
        fullTextQuery.setCacheable(true);
        Stream<_ProductItem> stream = fullTextQuery.stream();
        LinkedHashSet<Long> cells = new LinkedHashSet<>();
        stream.forEach(productItem -> cells.addAll(productItem.getCells_id()));
        return cells;
    }

    @Override
    public LinkedHashSet<Long> searchCarriagesByProduct(WarehouseFilterRequest request) {
        SearchFilter searchFilter = new SearchFilter();
        if (!StringUtils.isEmpty(request.getTerm())) {
            SearchFilter temp = new SearchFilter();
            temp.and("name", request.getTerm().toLowerCase().trim() + "*");
            temp.or("accountingCode", request.getTerm().toLowerCase().trim());
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (request.getInventarizationDate() != null) {
            SearchFilter temp = new SearchFilter();
            temp.and(String.format("inventarizationDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.DAY),
                    DateTools.dateToString(request.getInventarizationDate(), DateTools.Resolution.DAY)));
            temp.or("inventarizationLog:null");
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (request.getId() != null)
            searchFilter.and("warehouse.id", "" + request.getId());
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.PRODUCT_INVALID);
//        searchFilter.and("valid:true");

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ProductItem.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setSort(new Sort(new SortField("inventarizationDate", SortField.Type.STRING)));
        fullTextQuery.setCacheable(true);
        Stream<_ProductItem> stream = fullTextQuery.stream();
        LinkedHashSet<Long> cells = new LinkedHashSet<>();
        stream.forEach(productItem -> cells.addAll(productItem.getCarriages_id()));
        return cells;
    }

    @Override
    public _ProductItem save(_ProductItem entity) {
        if (entity == null)
            return entity;
        super.save(entity);
        if (entity.getQrcode() == null) {
            entity.setQrcode(entity.getId());
            super.save(entity);
        }

        if (entity.getPartition() != null) {
            partitionService.reCalculate(entity.getPartition());
        }
        return entity;
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

        public ProductItemDaoImpl.CustomFilter invoke() {
            filterQuery = "";
            params = preparing();
            if (filter.has("productId")) {
                filterQuery += " and t.product.id = :productId ";
                params.put("productId", filter.getLong("productId"));
            }
            if (filter.has("withOutstatus")) {
                filterQuery += " and t.state != 'USED' ";
            }
            return this;
        }
    }
}