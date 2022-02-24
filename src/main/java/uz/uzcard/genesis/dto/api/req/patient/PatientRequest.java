package uz.uzcard.genesis.dto.api.req.patient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PatientRequest implements Serializable {

    private Long id;
    private String patientFio;
    private String pasNumber;
    private String diagnosis;
    private String conclusion;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
}
