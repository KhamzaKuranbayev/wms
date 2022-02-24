package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Supplier;
import uz.uzcard.genesis.service.SupplierService;

import java.util.List;
import java.util.stream.Collectors;

@Api("Supplier controller")
@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @ApiOperation(value = "Supplier list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(SupplierFilterRequest request) {
        return ListResponse.of(supplierService.list(request), (supplier, map) -> map);
    }

    @ApiOperation(value = "Supplier Items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse items(SupplierFilterRequest request) {
        PageStream<_Supplier> pageStream = supplierService.list(request);
        List<SelectItem> list = pageStream.stream().map(supplier -> new SelectItem(supplier.getId(), supplier.getName(), "" + supplier.getId())).collect(Collectors.toList());
        return ListResponse.of(list, pageStream.getSize());
    }

    @ApiOperation(value = "Supplier save")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody SupplierRequest request) {
        return SingleResponse.of(supplierService.save(request), (supplier, map) -> map);
    }

    @ApiOperation(value = "Supplier save")
    @Transactional
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(@RequestBody DeleteRequest request) {
        supplierService.delete(request);
        return SingleResponse.empty();
    }
}
