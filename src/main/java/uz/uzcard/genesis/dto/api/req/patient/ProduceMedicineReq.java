package uz.uzcard.genesis.dto.api.req.patient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * @author norboboyev_h
 * @date 03.02.2021  18:06
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProduceMedicineReq implements Serializable {
    private Long medicineId;
    private double count;
}
