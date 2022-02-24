package uz.uzcard.genesis.dto.api.req.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TeamFilterRequest extends FilterBase {

    private Long id;
    private String name;
    private List<Long> departmentIds;
}
