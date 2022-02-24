package uz.uzcard.genesis.controller.contract;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.lot.LotFilterRequest;
import uz.uzcard.genesis.dto.api.req.partition.LotAddRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.enums.WarehouseReceivedType;
import uz.uzcard.genesis.service.LotService;
import uz.uzcard.genesis.uitls.ServerUtils;

@Api(value = "Lot controller", description = "Lot")
@RestController
@RequestMapping("/api/lot")
public class LotController {
    @Autowired
    private LotService lotService;

    @ApiOperation(value = "OutGoing Contract list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(LotFilterRequest request) {
        return ListResponse.of(lotService.list(request), (lot, map) -> {
            if (lot.getPartition() != null && lot.getPartition().getDate() != null) {
                map.add("partition.id", lot.getPartition().getId());
                map.add("partitionDate", lot.getPartition().getDate().format(ServerUtils.dateFormat));
            }
            return map;
        });
    }

    @ApiOperation(value = "Add lot")
    @Transactional
    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse add(@RequestBody LotAddRequest request) {
        return SingleResponse.of(lotService.add(request, WarehouseReceivedType.LOT, false), ((lot, map) -> map));
    }

    @ApiOperation(value = "Print qr code")
    @Transactional
    @GetMapping(value = "/print", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse print(Long id) {
        return ListResponse.of(lotService.print(id));
    }

}