package uz.uzcard.genesis.controller.contract;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.partition.LotAddRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionFilterRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionProductRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.entity._Lot;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.hibernate.enums.WarehouseReceivedType;
import uz.uzcard.genesis.service.LotService;
import uz.uzcard.genesis.service.PartitionService;
import uz.uzcard.genesis.service.ProductItemService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

/**
 * Created by norboboyev_h  on 14.08.2020  15:23
 */
@Api(value = "Partition controller", description = "Partition")
@RestController
@RequestMapping(value = "/api/partition")
public class PartitionController {

    @Autowired
    private PartitionService partitionService;
    @Autowired
    private LotService lotService;
    @Autowired
    private ProductItemService productItemService;

    @ApiOperation(value = "Partition list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(PartitionFilterRequest request) {
        return ListResponse.of(partitionService.list(request), (partition, map) -> {
            map.add("wareHouseName", partition.getWarehouse().getNameByLanguage());
            map.add("wareHouseAdress", partition.getWarehouse().getAddress());
            if (partition.getProduct().getGroup() != null)
                map.add("productCategory", partition.getProduct().getGroup().getName());
            if (partition.getProduct().getType() != null)
                map.add("productType", partition.getProduct().getType().getName());
            if (partition.getContractItem() != null && partition.getContractItem().getParent() != null)
                map.add("contractCode", partition.getContractItem().getParent().getCode());
            map.add("productName", partition.getProduct().getName());
            return map;
        });
    }

    @ApiOperation(value = "Get Products address by partition")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(@RequestParam(value = "partitionId") Long partitionId) {
        // todo mobile uchun qilingan, productid kelishi xisobiga og'irlashtirib qo'ymiydi.
        return ListResponse.of(productItemService.getProductItemAddress(partitionId));
    }

    @ApiOperation(value = "Partition add")
    @Transactional
    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse add(@RequestBody PartitionRequest request) {
        _Lot lot = lotService.add(new LotAddRequest() {{
            setContractItemId(request.getContractItemId());
            setCount(request.getCount());
            setDate(request.getDate());
            setProductCreationDate(request.getProductCreationDate());
            setLotName(null);
            setPackageCount(request.getPackageCount());
            setActNo(request.getActNo());
            setGtd(request.getGtd());
            setInvoiceDate(request.getInvoiceDate());
            setInvoiceNo(request.getInvoiceNo());
            setWarehouseId(request.getWarehouseId());
        }}, WarehouseReceivedType.PARTITION, false);
        return SingleResponse.of(lot, (lot1, map) -> {
            if (lot.getPartition() != null && lot.getPartition().getDate() != null) {
                map.add("partition.id", lot.getPartition().getId());
                map.add("partitionDate", lot.getPartition().getDate().format(ServerUtils.dateFormat));
            }
            return map;
        });
    }

    @ApiOperation(value = "Partition Produce")
    @Transactional
    @PostMapping(value = "/produce", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse produce(@RequestBody PartitionProductRequest request) {
        return partitionService.produce(request);
    }
}