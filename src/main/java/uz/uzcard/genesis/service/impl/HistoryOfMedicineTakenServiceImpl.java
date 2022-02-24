package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.patient.HistoryOfMedicineTakenFilterRequest;
import uz.uzcard.genesis.dto.api.req.patient.HistoryOfMedicineTakenRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignsRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.HistoryOfMedicineTakenService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.*;

@Service
public class HistoryOfMedicineTakenServiceImpl implements HistoryOfMedicineTakenService {

    @Autowired
    private HistoryOfMedicineTakenDao historyOfMedicineTakenDao;
    @Autowired
    private PatientDao patientDao;
    @Autowired
    private UnitTypeDao unitTypeDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private ProductDao productDao;

    @Override
    public PageStream<_HistoryOfMedicineTaken> list(HistoryOfMedicineTakenFilterRequest filter) {
        return historyOfMedicineTakenDao.search(new FilterParameters() {{
            if (filter.getMedicineId() != null)
                add("medicineId", "" + filter.getMedicineId());

            if (filter.getPatientId() != null)
                add("patientId", "" + filter.getPatientId());

            setStart(filter.getPage() * filter.getLimit());
            setSize(filter.getLimit());
        }});
    }

    @Override
    public PageStream<_HistoryOfMedicineTaken> holdOn(HistoryOfMedicineTakenFilterRequest filter) {
        _User user = SessionUtils.getInstance().getUser();
        if (user == null)
            throw new RpcException("USER_NOT_FOUND");

        return historyOfMedicineTakenDao.search(new FilterParameters() {{
            add("sessionId", "" + user.getId());
            if (filter.getMedicineId() != null)
                add("medicineId", "" + filter.getMedicineId());

            if (filter.getPatientId() != null)
                add("patientId", "" + filter.getPatientId());

            setStart(filter.getPage() * filter.getLimit());
            setSize(filter.getLimit());
        }});
    }

    @Override
    public SingleResponse save(HistoryOfMedicineTakenRequest request) {
        if (request.getPatientId() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PATIENT_REQUIRED"));
        _Patient patient = patientDao.get(request.getPatientId());
        if (patient == null)
            throw new RpcException(GlobalizationExtentions.localication("PATIENT_NOT_FOUND"));

        if (request.getUnitType() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("UNITTYPE_REQUIRED"));
        _UnitType unitType = unitTypeDao.getById(request.getUnitType());
        if (unitType == null)
            throw new RpcException(GlobalizationExtentions.localication("UNITTYPE_NOT_FOUND"));

        if (request.getMedicineId() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("MEDICINE_REQUIRED"));
        _ProductItem medicine = productItemDao.get(request.getMedicineId());
        if (medicine == null)
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));

        if (SessionUtils.getInstance().getUser() == null)
            throw new RpcException(GlobalizationExtentions.localication("USER_NOT_FOUND"));

        _HistoryOfMedicineTaken historyOfMedicineTaken =
                historyOfMedicineTakenDao.getByPatientAndMedicineAndState(patient.getId(), medicine.getId(), SessionUtils.getInstance().getUserId(), _State.ON_HOLD);
        if (historyOfMedicineTaken == null) {
            historyOfMedicineTaken = new _HistoryOfMedicineTaken();
            historyOfMedicineTaken.setCount(request.getCount());
        } else {
            historyOfMedicineTaken.setCount(historyOfMedicineTaken.getCount() + request.getCount());
        }

        historyOfMedicineTaken.setMedicine(medicine);
        historyOfMedicineTaken.setUnitType(unitType);
        historyOfMedicineTaken.setPatient(patient);
        historyOfMedicineTaken.setDoctor(SessionUtils.getInstance().getUser());
        historyOfMedicineTaken = historyOfMedicineTakenDao.save(historyOfMedicineTaken);
        return SingleResponse.of(historyOfMedicineTaken, (historyOfMedicineTaken1, map) -> map);
    }

    @Override
    public Long count(Long patientId) {
        _User user = SessionUtils.getInstance().getUser();
        if (user == null)
            throw new RpcException("USER_NOT_FOUND");
        if (patientId == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PATIENT_REQUIRED"));
        return historyOfMedicineTakenDao.getCount(patientId, user.getId());
    }

    @Override
    public SingleResponse produce(List<Long> ids) {
        if (ids == null)
            throw new ValidatorException("HISTORY_OF_MEDICINE_TAKEN_REQUIRED");

        Set<Long> patients = new HashSet<>();
        ids.forEach(historyId -> {
            _HistoryOfMedicineTaken historyOfMedicineTaken = historyOfMedicineTakenDao.get(historyId);
            if (historyOfMedicineTaken == null)
                throw new RpcException(GlobalizationExtentions.localication("HISTORY_OF_MEDICINE_TAKEN_NOT_FOUND"));

            produceMedicine(historyOfMedicineTaken.getMedicine(), historyOfMedicineTaken.getCount());

            historyOfMedicineTaken.setState(_State.TAKEN_AWAY);
            historyOfMedicineTaken.setTakenAwayDate(new Date());
            historyOfMedicineTakenDao.save(historyOfMedicineTaken);
            patients.add(historyOfMedicineTaken.getPatient().getId());
        });

        patients.forEach(patientId -> {
            _Patient patient = patientDao.get(patientId);
            if (patient == null)
                throw new RpcException(GlobalizationExtentions.localication("PATIENT_NOT_FOUND"));
            patient.setLastTakenAwayDate(new Date());
            patientDao.save(patient);
        });
        return SingleResponse.of(true);
    }

    @Override
    public SingleResponse removeHold(Long id) {
        if (id == null)
            throw new ValidatorException("HISTORY_OF_MEDICINE_TAKEN_REQUIRED");
        _HistoryOfMedicineTaken historyOfMedicineTaken = historyOfMedicineTakenDao.get(id);
        if (historyOfMedicineTaken == null)
            throw new RpcException(GlobalizationExtentions.localication("HISTORY_OF_MEDICINE_TAKEN_NOT_FOUND"));
        historyOfMedicineTaken.setState(_State.DELETED);
        historyOfMedicineTakenDao.save(historyOfMedicineTaken);

        _ProductItem productItem = productItemDao.get(historyOfMedicineTaken.getMedicine().getId());
        if (productItem == null)
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
        productItem.setCount(productItem.getCount() + historyOfMedicineTaken.getCount());
        productItemDao.save(productItem);
        return SingleResponse.of(true);
    }

    @Override
    public SingleResponse setHashESign(HashESignsRequest request) {
        if (request.getHashESign() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("HASH_CODE_REQUIRED"));
        if (request.getIds() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("HISTORY_OF_MEDICINE_TAKEN_REQUIRED"));

        request.getIds().forEach(id -> {
            _HistoryOfMedicineTaken historyOfMedicineTaken = historyOfMedicineTakenDao.get(id);
            if (historyOfMedicineTaken == null)
                throw new RpcException(GlobalizationExtentions.localication("HISTORY_OF_MEDICINE_TAKEN_NOT_FOUND"));
            historyOfMedicineTaken.setHashESign(request.getHashESign());
            historyOfMedicineTakenDao.save(historyOfMedicineTaken);
        });
        return SingleResponse.of(true);
    }

    private void produceMedicine(_ProductItem productItem, Double count) {
        if (productItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("MEDICINE_NOT_FOUND"));
        if (productItem.getCount() - count < 0)
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_IS_NOT_ENOUGH"));
        productItem.setCount(productItem.getCount() - count);
        productItemDao.save(productItem);
    }


}
