package uz.uzcard.genesis.controller.patient;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.patient.HistoryOfMedicineTakenFilterRequest;
import uz.uzcard.genesis.dto.api.req.patient.HistoryOfMedicineTakenRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignsRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.HistoryOfMedicineTakenService;

import java.util.List;

@Api(value = "HistoryOfMedicineTaken controller")
@RestController
@RequestMapping(value = "/api/historyOfMedicineTaken")
public class HistoryOfMedicineTakenController {

    @Autowired
    private HistoryOfMedicineTakenService historyOfMedicineTakenService;

    @ApiOperation(value = "HistoryOfMedicineTaken save")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody HistoryOfMedicineTakenRequest request) {
        return historyOfMedicineTakenService.save(request);
    }

    @ApiOperation(value = "HistoryOfMedicineTaken list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(HistoryOfMedicineTakenFilterRequest request) {
        return ListResponse.of(historyOfMedicineTakenService.list(request), ((log, map) -> {
            if (log.getAuditInfo() != null && log.getAuditInfo().getCreatedByUser() != null)
                map.addString("givenBy", log.getDoctor().getShortName());
            if (log.getMedicine() != null) {
                map.addString("medicineId", "" + log.getMedicine().getId());
                map.addString("medicineName", "" + log.getMedicine().getName());
            }
            if (log.getUnitType() != null) {
                map.add("unitTypeId", log.getUnitType().getId());
                map.add("unit_type_name_en", log.getUnitType().getNameEn());
                map.add("unit_type_name_ru", log.getUnitType().getNameRu());
                map.add("unit_type_name_uz", log.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", log.getUnitType().getNameCyrl());
            }
            if (log.getPatient() != null) {
                map.addString("takenAwayBy", log.getPatient().getFio());
                map.addString("conclusion", log.getPatient().getConclusion());
                map.addString("diagnosis", log.getPatient().getDiagnosis());
            }
            return map;
        }));
    }

    @ApiOperation(value = "HistoryOfMedicineTaken list hold on")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/on-hold", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse holdOn(HistoryOfMedicineTakenFilterRequest request) {
        return ListResponse.of(historyOfMedicineTakenService.holdOn(request), ((log, map) -> {
            if (log.getAuditInfo() != null && log.getAuditInfo().getCreatedByUser() != null)
                map.addString("givenBy", log.getDoctor().getShortName());
            if (log.getMedicine() != null) {
                map.addString("medicineId", "" + log.getMedicine().getId());
                map.addString("medicineName", "" + log.getMedicine().getName());
            }
            if (log.getUnitType() != null) {
                map.add("unitTypeId", log.getUnitType().getId());
                map.add("unit_type_name_en", log.getUnitType().getNameEn());
                map.add("unit_type_name_ru", log.getUnitType().getNameRu());
                map.add("unit_type_name_uz", log.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", log.getUnitType().getNameCyrl());
            }
            if (log.getPatient() != null)
                map.addString("takenAwayBy", log.getPatient().getFio());
            return map;
        }));
    }

    @ApiOperation(value = "HistoryOfMedicineTaken count")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse count(Long patientId) {
        return SingleResponse.of(historyOfMedicineTakenService.count(patientId));
    }

    @ApiOperation(value = "HistoryOfMedicineTaken produce")
    @Transactional
    @PostMapping(value = "/save-produce", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse saveProduce(@RequestBody List<Long> ids) {
        return historyOfMedicineTakenService.produce(ids);
    }

    @ApiOperation(value = "HistoryOfMedicineTaken remove hold")
    @Transactional
    @PostMapping(value = "/remove-hold", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse removeHold(@RequestBody Long id) {
        return historyOfMedicineTakenService.removeHold(id);
    }

    @Transactional
    @ApiOperation(value = "Set Hash ESign")
    @PostMapping(value = "/set-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkHashESign(@RequestBody HashESignsRequest request) {
        return historyOfMedicineTakenService.setHashESign(request);
    }
}
