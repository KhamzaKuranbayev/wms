package uz.uzcard.genesis.service.impl;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.partition.PartitionCarriageAddressDto;
import uz.uzcard.genesis.dto.api.req.warehouse.*;
import uz.uzcard.genesis.exception.CriticException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.PlacementType;
import uz.uzcard.genesis.service.CarriageService;
import uz.uzcard.genesis.service.OrderItemsService;
import uz.uzcard.genesis.service.PartitionService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.StateConstants;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 19.08.2020  17:55
 */
@Service
public class CarriageServiceImpl implements CarriageService {

    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private CarriageDao carriageDao;
    @Autowired
    private StillageColumnDao stillageColumnDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private Gson gson;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private PartitionService partitionService;
    @Autowired
    private WarehouseYDao warehouseYDao;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private StillageDao stillageDao;

    @Override
    public PageStream<_Carriage> list(CarriageFilterRequest request) {
        return carriageDao.search(new FilterParameters() {{
            if (!ServerUtils.isEmpty(request.getStillageColumnId())) {
                add("stillageColumnId", "" + request.getStillageColumnId());
            }
            if (!ServerUtils.isEmpty(request.getAllSearch())) {
                add("allSearch", request.getAllSearch());
            }
        }});
    }

    @Override
    public _Carriage getSingle(@NotNull Long id) {
        _Carriage carriage = carriageDao.get(id);
        if (carriage == null)
            throw new ValidatorException(GlobalizationExtentions.localication("CARRIAGE_NOT_FOUND"));
        return carriageDao.get(id);
    }

    @Override
    public _Carriage create(_StillageColumn stillageColumn, CarriageRequest request) {
//        if (!ServerUtils.isEmpty(request.getStillageColumnId())) {
//        _StillageColumn stillageColumn = stillageColumnDao.get(request.getStillageColumnId());
        _Carriage carriage = new _Carriage();
        if (!ServerUtils.isEmpty(stillageColumn)) {
            carriage.setRemainedCapacityPercentage(100);
            carriage.setStillageColumn(stillageColumn);
            carriage.setDepth(request.getCarriageDepth());
            carriage.setHeight(request.getCarriageHeight());
            carriage.setWidth(request.getCarriageWidth());
            carriage.setSortOrder(carriageDao.getMaxOrderByColumn(stillageColumn) + 1);
            if (!request.isPreview())
                carriageDao.save(carriage);
        }
        return carriage;
//        }
//        throw new ValidatorException("StillageColumnId Id null bo'lmasligi kerak");
    }

    @Override
    public void put(PutToCarriageRequest request) {
        if (request.getProductItemIds() == null || request.getProductItemIds().isEmpty())
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCTS_IS_REQUIRED"));
        List<_Carriage> oldCarriagesList = new ArrayList<>();
        if (!(request.getCarriageIds() == null || request.getCarriageIds().isEmpty())) {

            List<_Carriage> carriages = carriageDao.findByIds(request.getCarriageIds()).collect(Collectors.toList());
            for (_Carriage carriage : carriages) {
//                carriage.setRemainedCapacityPercentage(remainedCapacityPercent(request, carriage));
                carriage.setHasProduct(true);
                carriageDao.save(carriage);
            }
            productItemDao.findByQrCodes(request.getProductItemIds()).collect(Collectors.toList()).forEach(productItem -> {
                if (!ServerUtils.isEmpty(productItem)) {

                    if (StateConstants.PRODUCT_PRODUCED.equals(productItem.getState()))
                        throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_TAKEN_FROM_WAREHOUSE"));
                    List<Long> oldCarriagesId = productItem.getCarriages_id();
                    if (productItem.getPlacementType() == null)
                        productItem.setPlacementType(PlacementType.PLACED);
                    if (!ServerUtils.isEmpty(oldCarriagesId) && productItem.getPlacementType() != null && !productItem.getPlacementType().equals(PlacementType.REPLACED))
                        productItem.setPlacementType(PlacementType.REPLACED);
                    productItem.setCells_id(warehouseYDao.findCellsByCarriage(request.getCarriageIds()));

                    productItem.setCarriages_id(carriages.stream().map(_Carriage::getId).collect(Collectors.toList()));
                    productItem.setPlacedDate(new Date());
                    productItem.setPlacedBy(carriageDao.getUser());
                    _Carriage carriage = carriages.get(0);
                    if (carriage != null && carriage.getStillageColumn() != null && carriage.getStillageColumn().getStillage() != null) {
                        productItem.setWarehouse(carriage.getStillageColumn().getStillage().getWarehouse());
                        _Partition partition = productItem.getPartition();
                        if (partition != null) {
                            partition.setWarehouse(carriage.getStillageColumn().getStillage().getWarehouse());
                            partitionDao.save(partition);
                        }
                    }
                    productItemDao.save(productItem);
                    if (productItem.getPartition() == null)
                        throw new CriticException(GlobalizationExtentions.localication("PARTION_NOT_FOUND"));
                    if (productItem.getPartition().getContractItem() != null) {
                        List<_OrderItem> orderItems = orderItemDao.findByContractItemAndByNotStates(productItem.getPartition().getContractItem(),
                                _State.DELETED, _State.READY_TO_PRODUCE, _State.YES_PRODUCT, _State.RECEIVED);
                        if (!orderItems.isEmpty()) {
                            orderItems.forEach(orderItem -> orderItemsService.changeStatus(orderItem, _State.YES_PRODUCT));
                        }
                    }
                    List<_Carriage> oldCarriages = carriageDao.findByIds(oldCarriagesId).collect(Collectors.toList());
                    checkToHasProduct(oldCarriages);
                    oldCarriagesList.addAll(oldCarriages);
                } else throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
            });
            carriages.stream().map(carriage -> carriage.getStillageColumn().getStillage().getWarehouse())
                    .distinct().forEach(warehouse -> {
                warehouse.setOccupancyPercent(carriageDao.totalCarriageCountyWarehouse(warehouse));
                warehouseDao.save(warehouse);
            });
            oldCarriagesList.stream().map(carriage -> carriage.getStillageColumn().getStillage().getWarehouse())
                    .distinct().forEach(warehouse -> {
                warehouse.setOccupancyPercent(carriageDao.totalCarriageCountyWarehouse(warehouse));
                warehouseDao.save(warehouse);
            });

        } else throw new ValidatorException(GlobalizationExtentions.localication("CARRIAGE_IS_REQUIRED"));

    }

    @Override
    public Boolean full(CarriageIsFullRequest request) {
        if (!ServerUtils.isEmpty(request.getCarriageId())) {
            _Carriage carriage = carriageDao.get(request.getCarriageId());
            if (!ServerUtils.isEmpty(carriage)) {
                if (request.isFull()) {
//                    carriage.setRemainedCapacityPercentage(100);
                    carriage.setFull(request.isFull());
                } else carriage.setFull(request.isFull());
                carriageDao.save(carriage);

                //warehouse percent logics
                if (carriage.getStillageColumn() != null && carriage.getStillageColumn().getStillage() != null
                        && carriage.getStillageColumn().getStillage().getWarehouse() != null) {
                    _Warehouse warehouse = carriage.getStillageColumn().getStillage().getWarehouse();
                    warehouse.setOccupancyPercent(carriageDao.totalCarriageCountyWarehouse(warehouse));
                    warehouseDao.save(warehouse);
                }
            } else throw new ValidatorException(GlobalizationExtentions.localication("CARRIAGE_NOT_FOUND"));
            return true;
        }
        throw new ValidatorException(GlobalizationExtentions.localication("CARRIAGE_IS_REQUIRED"));
    }

    @Override
    public void checkToHasProduct(List<_Carriage> carriages) {
        if (carriages != null)
            carriages.forEach(carriage -> {
                boolean hasProduct = productItemDao.hasProductItemByCarriage(carriage);
                if (!hasProduct) {
                    carriage.setFull(false);
                }
                carriage.setHasProduct(hasProduct);
                carriageDao.save(carriage);
            });
    }

    @Override
    public PartitionCarriageAddressDto getAddress(_Carriage carriage) {
        PartitionCarriageAddressDto addressDto = new PartitionCarriageAddressDto();
        _StillageColumn column = carriage.getStillageColumn();
        if (!ServerUtils.isEmpty(column)) {
            _Stillage stillage = column.getStillage();
            if (!ServerUtils.isEmpty(stillage)) {
                addressDto.setCarriage(carriage.getId());
                addressDto.setCarriagePosition(carriage.getSortOrder());
                addressDto.setStillageColumn(column.getId());
                addressDto.setStillageColumnName(column.getCode());
                addressDto.setStillage(stillage.getId());
                addressDto.setStillageName(stillage.getName());

                String builder = stillage.getName() + " " +
                        column.getCode() +
                        carriage.getSortOrder();
                addressDto.setAddresse(builder);
                return addressDto;
            }
        }
        return null;
    }

    @Override
    public PageStream<_Carriage> getForMarkingAsFullOrNot(List<Long> ids) {
        // todo bu yerda carriagelarning idlari keladi lekin bu funksiya qanday holatda ishlatilishini bilmaganligim uchun exception qaytarmadim
        if (ids == null || ids.isEmpty())
            throw new ValidatorException("ids polya null bo'lmasligi kerak");

        List<_Carriage> carriages = carriageDao.findByIds(ids).collect(Collectors.toList());

        return new PageStream<>(carriages.stream(), carriages.size());
    }

    @Override
    public void delete(Long id) {
        _Carriage carriage = carriageDao.get(id);
        if (ServerUtils.isEmpty(carriage)) {
            throw new ValidatorException("CARRIAGE_IS_NULL");
        }
        if (carriage.isHasProduct())
            throw new ValidatorException("Бу ерда продукт бор");
        carriageDao.delete(id);
        _StillageColumn column = carriage.getStillageColumn();
        if (column == null)
            throw new CriticException("Ушбу каретка ҳеч бир стиллажга тегишли эмас");
        _Stillage stillage = column.getStillage();
        if (stillage == null)
            throw new CriticException("Стиллажга топилмади");
        stillageColumnDao.save(column);
        stillageDao.save(stillage);
    }

    @Override
    public List<PartitionCarriageAddressDto> getAddresses(List<Long> carriages_ids) {
        return carriageDao.findByIds(carriages_ids).map(this::getAddress).collect(Collectors.toList());
    }

    @Override
    public Stream<_Carriage> findByIds(List<Long> carriagesId) {
        return carriageDao.findByIds(carriagesId);
    }

    @Override
    public LinkedHashSet<Long> searchByProduct(WarehouseFilterRequest request) {
        return productItemDao.searchCarriagesByProduct(request);
    }

    @Override
    public boolean setupSize(CarriageSizeRequest request) {
        carriageDao.findByIds(request.getIds()).forEach(carriage -> {
            if (request.getWidth() != null) {
                carriage.setWidth(request.getWidth());
            }
            if (request.getHeight() != null) {
                carriage.setHeight(request.getHeight());
            }
            carriageDao.save(carriage);
        });
        return true;
    }

//    public Integer remainedCapacityPercent(PutToCarriageRequest request, _Carriage carriage) {
//        Double capacity = Double.valueOf(carriage.getDepth() * carriage.getHeight() * carriage.getWidth());
//        Double productCapacity = Double.valueOf(request.getDepth() * request.getHeight() * request.getWidth());
//        if ((capacity * (carriage.getRemainedCapacityPercentage() / 100.0)) < productCapacity || capacity < productCapacity) {
//            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_DID_NOT_TO_MATCH"));
//        }
//        return Integer.valueOf(carriage.getRemainedCapacityPercentage() - Math.toIntExact(Math.round((productCapacity / (capacity * carriage.getRemainedCapacityPercentage() / 100.0)) * 100.0)));
//    }
}
