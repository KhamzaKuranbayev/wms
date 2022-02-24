package uz.uzcard.genesis.dto.api.req.patient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class HistoryOfMedicineTakenRequest implements Serializable {

    private Long patientId;
    private Long medicineId;
    private Long unitType;
    private Double count;
}
