package uz.uzcard.genesis.dto.api.req.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 07.08.2020  11:27
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserAgreementRequest implements Serializable {
    private Long contractItemId;
    private Long userId;
    private boolean ozl;
}