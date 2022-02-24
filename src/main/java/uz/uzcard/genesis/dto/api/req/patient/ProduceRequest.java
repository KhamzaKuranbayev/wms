package uz.uzcard.genesis.dto.api.req.patient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @author norboboyev_h
 * @date 03.02.2021  18:04
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProduceRequest implements Serializable {
    private Long departmentId;
    private List<ProduceMedicineReq> produceMedicine;
}
