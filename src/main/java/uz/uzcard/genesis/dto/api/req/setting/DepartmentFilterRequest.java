package uz.uzcard.genesis.dto.api.req.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DepartmentFilterRequest extends FilterBase {

    private String name;

    private Long parentId;

    private boolean byCurrentUserTeams;
}
