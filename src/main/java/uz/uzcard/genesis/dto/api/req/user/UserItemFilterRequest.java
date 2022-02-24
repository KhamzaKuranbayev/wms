package uz.uzcard.genesis.dto.api.req.user;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class UserItemFilterRequest extends FilterBase {
    private String name;
}
