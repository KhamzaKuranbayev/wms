package uz.uzcard.genesis.controller.order;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.ProduceHistoryFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.ProduceHistoryService;

/**
 * Created by norboboyev_h  on 25.12.2020  17:53
 */

@Api(value = "Produce History controller")
@RestController
@RequestMapping(value = "/api/produce-history")
public class ProduceHistoryController {
    private final ProduceHistoryService produceHistoryService;

    public ProduceHistoryController(ProduceHistoryService produceHistoryService) {
        this.produceHistoryService = produceHistoryService;
    }

    @ApiOperation(value = "Produce History List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(ProduceHistoryFilter filter) {
        return ListResponse.of(produceHistoryService.list(filter), ((produceHistory, map) -> {
            if (produceHistory.getAttachment() != null)
                map.add("filePath", produceHistory.getAttachment().getName());
            return map;
        }));
    }

    @ApiOperation(value = "Mark as seen")
    @Transactional
    @PostMapping(value = "/mark-as-seen/{orderItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse generateQrCode(@PathVariable Long orderItemId) {
        produceHistoryService.markAsSeen(orderItemId);
        return SingleResponse.empty();
    }
}
