package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * @author Javohir Elmurodov
 * @created 25/09/2020 - 11:09 AM
 * @project GTL
 */

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ChangePasswordRequest implements Serializable {
    private String username;
    private String oldPassword;
    private String newPassword;
}
