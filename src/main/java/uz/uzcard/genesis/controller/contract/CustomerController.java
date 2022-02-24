package uz.uzcard.genesis.controller.contract;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.contract.CustomerFilterRequest;
import uz.uzcard.genesis.dto.api.req.contract.CustomerRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.CustomerService;

import javax.validation.Valid;

@Api
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @ApiOperation(value = "OutGoing Contract list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(CustomerFilterRequest request) {
        return customerService.list(request);
    }

    @ApiOperation(value = "Save customer")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@Valid CustomerRequest request) {
        return SingleResponse.of(customerService.save(request), (customer, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Delete customer")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        customerService.delete(id);
        return SingleResponse.empty();
    }
}
