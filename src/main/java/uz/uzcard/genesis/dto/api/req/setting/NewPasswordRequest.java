package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 02.07.2020  17:57
 */
@Getter
@Setter
public class NewPasswordRequest implements Serializable {

    private String newPassword;

    private String username;

}
