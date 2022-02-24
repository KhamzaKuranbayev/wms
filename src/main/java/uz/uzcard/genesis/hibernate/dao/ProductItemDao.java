package uz.uzcard.genesis.hibernate.dao;

import org.hibernate.search.query.facet.Facet;
import uz.uzcard.genesis.dto.api.req.WarehouseSearchEngineFilterRequest;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseFilterRequest;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity.*;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface ProductItemDao extends Dao<_ProductItem> {

    LinkedList<Long> getPackageTypesByProduct(Long product_id);

    LinkedList<Long> getPackageByProjectAndPackageType(Long product_id, Long packageType_id);

    Stream<_ProductItem> findAllByPartition(_Partition partition);

    Stream<_ProductItem> findByQrCodes(List<Long> ids);

    Stream<_ProductItem> findByIds(List<Long> ids);

    Stream<_ProductItem> findAllByIds(List<Long> ids);

    Double findCountByIds(List<Long> ids);

    Double findCountByPartition(_Partition partition, boolean isAll);

    Stream<_ProductItem> findByPartition(_Partition partition);

    List<Long> findIdsByPartition(_Partition partition);

    List<Long> findIdsByPartitionId(Long partitionId);

    boolean hasProductItemByCarriage(_Carriage carriage);

    Stream<_ProductItem> findByLot(Long lotId);

    double getCountByLot(_Lot lot);

    double getRemainsByLot(_Lot lot);

    _ProductItem getByAccountingCode(String accountingCode);

    _ProductItem getByQrCode(Long qrCOde);

    _ProductItem getByQrCodeNewOnly(Long qrCode);

    List<Facet> searchWarehouseByProduct(WarehouseSearchEngineFilterRequest request);

    LinkedHashSet<Long> searchWarehouseYByProduct(WarehouseFilterRequest request);

    LinkedHashSet<Long> searchCarriagesByProduct(WarehouseFilterRequest request);

    Stream<_ProductItem> getForReturning(Set<_Warehouse> warehouse);

    List<Facet> facetByUserDepartment(DashboardFilter filterRequest);

    boolean hasAnyByWarehouse(Long warehouseId);

    Stream<_ProductItem> findAllByLot(_Lot lot);
}