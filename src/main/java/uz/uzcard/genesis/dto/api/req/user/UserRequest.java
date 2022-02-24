package uz.uzcard.genesis.dto.api.req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserRequest implements Serializable {

    private Long id;
    @ApiModelProperty(required = true)
    private String password;
    @ApiModelProperty(required = true)
    private String userName;

    private Long departmentId;

    private String firstName;
    private String lastName;
    private String middleName;
    private boolean forCurrentUser;
    @ApiModelProperty(required = true)
    private String phone;
    @ApiModelProperty(required = true)
    private String email;
    private boolean leader;

    private Set<Long> roles;
}
