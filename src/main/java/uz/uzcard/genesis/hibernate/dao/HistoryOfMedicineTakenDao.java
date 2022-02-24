package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._HistoryOfMedicineTaken;

public interface HistoryOfMedicineTakenDao extends Dao<_HistoryOfMedicineTaken> {

    Long getCount(Long patientId, Long sessionUserId);

    _HistoryOfMedicineTaken getByPatientAndMedicineAndState(Long patientId, Long medicineId, Long userId, String state);
}
