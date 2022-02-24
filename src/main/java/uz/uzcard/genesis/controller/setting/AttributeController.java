package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.AttributeFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.AttributeRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Attribute;
import uz.uzcard.genesis.service.AttributeService;

import javax.validation.Valid;

@Api(value = "Attributlar controlleri")
@RestController
@RequestMapping(value = "/api/attributes")
public class AttributeController {

    @Autowired
    private AttributeService attributeService;

    @ApiOperation(value = "Get items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(@Valid AttributeFilterRequest request) {
        PageStream<_Attribute> pageStream = attributeService.list(request);
        return ListResponse.of(pageStream, (attribute, map) -> {
            if (attribute.getItems() != null)
                map.addStrings("items", attribute.getItems());
            return map;
        });
    }

    @ApiOperation(value = "Save attribute")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody AttributeRequest request) {
        return SingleResponse.of(attributeService.save(request), (attribute, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Delete attribute")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        attributeService.delete(id);
        return SingleResponse.empty();
    }
}