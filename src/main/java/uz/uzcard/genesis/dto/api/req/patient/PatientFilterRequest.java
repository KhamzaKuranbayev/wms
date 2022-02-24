package uz.uzcard.genesis.dto.api.req.patient;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class PatientFilterRequest extends FilterBase {

    private Long id;
    private String passportNumber;
    private String fio;
}
