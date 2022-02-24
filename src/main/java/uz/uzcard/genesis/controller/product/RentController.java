package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.product.*;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.RentService;
import uz.uzcard.genesis.uitls.SessionUtils;

@Api(value = "Rent controller", description = "Mahsulotlarni ijaraga berish")
@RestController
@RequestMapping(value = "/api/rent")
public class RentController {

    @Autowired
    private RentService rentService;

    @ApiOperation(value = "Save rent", response = ListResponse.class)
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody RentRequest request) {
        return SingleResponse.of(rentService.save(request), (rent, map) -> {
            if (rent.getProductItem() != null)
                map.add("productItemId", rent.getProductItem().getId());
            if (rent.getProductItemParent() != null) {
                map.add("productItemParentId", rent.getProductItemParent().getId());
                if (rent.getProductItemParent().getProduct() != null)
                    map.add("productName", rent.getProductItemParent().getProduct().getName());
            }
            if (rent.getDepartment() != null)
                map.add("departmentId", rent.getDepartment().getId());
            return map;
        });
    }

    @ApiOperation(value = "Update rent", response = ListResponse.class)
    @Transactional
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse update(@RequestBody RentChangeStatusRequest request) {
        return SingleResponse.of(rentService.update(request), (rent, map) -> {
            if (rent.getProductItem() != null)
                map.add("productItemId", rent.getProductItem().getId());
            if (rent.getProductItemParent() != null) {
                map.add("productItemParentId", rent.getProductItemParent().getId());
                if (rent.getProductItemParent().getProduct() != null)
                    map.add("productName", rent.getProductItemParent().getProduct().getName());
            }
            if (rent.getDepartment() != null)
                map.add("departmentId", rent.getDepartment().getId());
            return map;
        });
    }

    @ApiOperation(value = "Get rent list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(RentFilterRequest request) {
        return ListResponse.of(rentService.search(request), (rent, map) -> {
            if (rent.getDepartment() != null) {
                map.add("departmentId", rent.getDepartment().getId());
                map.add("departmentName", rent.getDepartment().getNameByLanguage());
            }
            if (rent.getProductItem() != null) {
                map.add("productItemId", rent.getProductItem().getId());
            }
            if (rent.getProductItemParent() != null) {
                map.add("productItemParentId", rent.getProductItemParent().getId());
                if (rent.getProductItemParent().getProduct() != null)
                    map.add("productName", rent.getProductItemParent().getProduct().getName());
            }
            return map;
        });
    }
}
