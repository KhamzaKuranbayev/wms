package uz.uzcard.genesis.controller.patient;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.patient.PatientFilterRequest;
import uz.uzcard.genesis.dto.api.req.patient.PatientRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.PatientService;

@Api(value = "Patient controller")
@RestController
@RequestMapping(value = "/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @ApiOperation(value = "Patient list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(PatientFilterRequest request) {
        return ListResponse.of(patientService.list(request), ((patient, map) -> {
            return map;
        }));
    }

    @ApiOperation(value = "Patient add")
    @Transactional
    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse add(@RequestBody PatientRequest request) {
        return patientService.add(request);
    }
}
