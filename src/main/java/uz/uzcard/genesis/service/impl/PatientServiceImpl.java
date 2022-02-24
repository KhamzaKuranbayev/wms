package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.patient.PatientFilterRequest;
import uz.uzcard.genesis.dto.api.req.patient.PatientRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.PatientDao;
import uz.uzcard.genesis.hibernate.entity._Patient;
import uz.uzcard.genesis.service.PatientService;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientDao patientDao;

    @Override
    public PageStream<_Patient> list(PatientFilterRequest filter) {
        return patientDao.search(new FilterParameters() {{
            if (filter.getId() != null)
                add("id", "" + filter.getId());
            if (filter.getFio() != null)
                add("fio", filter.getFio());
            if (filter.getPassportNumber() != null)
                add("passportNumber", filter.getPassportNumber());
            setStart(filter.getPage() * filter.getLimit());
            setSize(filter.getLimit());
        }});
    }

    @Override
    public SingleResponse add(PatientRequest request) {
        _Patient patient;
        if (request.getId() == null) {
            patient = new _Patient();
        } else {
            patient = patientDao.get(request.getId());
        }

        patient.setFio(request.getPatientFio());
        patient.setPasNumber(request.getPasNumber());
        patient.setDiagnosis(request.getDiagnosis());
        patient.setConclusion(request.getConclusion());
        patient.setBirthday(request.getBirthday());
        patient = patientDao.save(patient);
        return SingleResponse.of(patient, (patient1, map) -> map);
    }

}
