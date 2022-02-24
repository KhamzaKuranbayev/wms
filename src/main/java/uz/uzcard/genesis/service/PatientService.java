package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.partition.PartitionFilterRequest;
import uz.uzcard.genesis.dto.api.req.patient.PatientFilterRequest;
import uz.uzcard.genesis.dto.api.req.patient.PatientRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Patient;

public interface PatientService {

    PageStream<_Patient> list(PatientFilterRequest filter);

    SingleResponse add(PatientRequest request);
}
