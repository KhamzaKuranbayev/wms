package uz.uzcard.genesis.dto.api.req.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserFilterRequest extends FilterBase {
    private String userName;
    private String firstName;
    private String lastName;

    private String phoneNumber;
    private String email;
    private Long departmentId;
    private List<String> roles;
    private OrderClassification depType;
    private boolean isDepartment;
}
