package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.StateRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.StateService;

@Api(value = "State controller")
@RequestMapping("/api/state")
@RestController
public class StateController {

    @Autowired
    private StateService stateService;

    @ApiOperation(value = "Tablitsalar bo`yicha statuslar ro`yxati")
    @GetMapping(value = "/listBy-entityName", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ListResponse listByEntityName(String entityName) {
        return ListResponse.of(stateService.listByEntityName(entityName), (state, map) -> map);
    }

    @ApiOperation(value = "Statusni saqlash")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody StateRequest request) {
        return SingleResponse.of(stateService.save(request), (state, map) -> map);
    }
}