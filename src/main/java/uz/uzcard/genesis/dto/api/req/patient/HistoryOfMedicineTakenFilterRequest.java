package uz.uzcard.genesis.dto.api.req.patient;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class HistoryOfMedicineTakenFilterRequest extends FilterBase {

    private Long patientId;
    private Long medicineId;
}
