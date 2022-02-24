package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.lot.LotFilterRequest;
import uz.uzcard.genesis.dto.api.req.partition.LotAddRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.WarehouseReceivedType;
import uz.uzcard.genesis.service.LotService;
import uz.uzcard.genesis.service.PartitionService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LotServiceImpl implements LotService {
    @Autowired
    private LotDao lotDao;
    @Autowired
    private PartitionService partitionService;
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private WarehouseDao warehouseDao;

    @Override
    public _Lot add(LotAddRequest request, WarehouseReceivedType warehouseReceivedType, boolean used) {
        _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
        if (contractItem == null)
            throw new ValidatorException("Контракт топилмади");
        if (request.getWarehouseId() == null)
            throw new ValidatorException("WAREHOUSE_IS_REQUIRED");
        _Warehouse warehouse = warehouseDao.get(request.getWarehouseId());
        if (warehouse == null)
            throw new RpcException(GlobalizationExtentions.localication("WAREHOUSE_NOT_FOUND"));

        double count = contractItem.getPartitions().stream().mapToDouble(_Partition::getCount).sum();
        if (count + Math.abs(request.getCount()) > contractItem.getAcceptedCount())
            throw new ValidatorException("Қабул қилинган махсулот ҳажмидан кўпроқ олиб кирмоқчисиз");
        _Lot lot = new _Lot();
        lot.setName(request.getLotName());
        lot.setPackageCount(request.getPackageCount());
        lot.setCount(request.getCount());
        lot.setRemains(request.getCount());
        lot.setActNo(request.getActNo());
        lot.setInvoiceDate(request.getInvoiceDate());
        lot.setInvoiceNo(request.getInvoiceNo());
        lot.setGtd(request.getGtd());
        lotDao.save(lot);
        Date expiration = null;
        if (contractItem.getProduct() != null
                && contractItem.getProduct().getExpiration() != null
                && request.getProductCreationDate() != null)
            expiration = Date.from(request.getProductCreationDate().toInstant().plusSeconds(contractItem.getProduct().getExpiration()));

        _Partition partition = partitionService.save(new PartitionRequest(request.getContractItemId(),
                        warehouse.getId(),
                        null,
                        request.getCount(),
                        request.getPackageCount(),
                        request.getDate(), expiration),
                lot.getId(), used);

        if (!partition.getLots().contains(lot))
            partition.getLots().add(lot);
        lot.setPartition(partition);
        lotDao.save(lot);
        contractItem.setActualReceiveDate(request.getDate());
        if (lot.getPartition() != null) {
            partitionDao.save(partition);
            if (partition.getContractItem() != null) {
                contractItem.setWarehouseReceivedType(warehouseReceivedType);
                contractItemDao.save(contractItem);
                contractItemDao.reindex(List.of(contractItem.getId()));
            }
            if (contractItem.getParent() != null) {
                contractDao.save(contractItem.getParent());
            }
        }
        return lot;
    }

    @Override
    public PageStream<_Lot> list(LotFilterRequest request) {
        return lotDao.search(new FilterParameters() {{
            setStart(0);
            setSize(Integer.MAX_VALUE);
            addLong("contractItemId", request.getContractItemId());
            addString("name", request.getName());
        }});
    }

    @Override
    public List<HashMap<String, String>> print(Long id) {
        _Lot lot = lotDao.get(id);
        if (lot == null)
            throw new ValidatorException("Лот топилмади");
        lot.setQrPrinted(true);
        lotDao.save(lot);
        List<HashMap<String, String>> returnedData = productItemDao.search(new FilterParameters() {{
            addLong("lotId", id);
            setSize(Integer.MAX_VALUE);
        }}).stream().map(productItem -> {
            CoreMap map = new CoreMap();
            map.add("qrCode", productItem.getQrcode());
            map.add("state", productItem.getState());
            return map.getInstance();
        }).collect(Collectors.toList());
        return returnedData;
    }

    @Override
    public void reCalculate(_Lot lot) {
        if (lot == null)
            return;
        lot.setCount(productItemDao.getCountByLot(lot));
        lot.setRemains(productItemDao.getRemainsByLot(lot));
        lotDao.save(lot);
    }
}