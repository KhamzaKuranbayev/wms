package uz.uzcard.genesis.dto.api.resp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzcard.genesis.dto.SelectItem;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardContractResponse {

    List<SelectItem> conclution;
    List<SelectItem> accepted;
}
