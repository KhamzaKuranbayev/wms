package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class WarehouseFilterRequest extends FilterBase {
    private Long id;
    private String name;
    private String term;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date inventarizationDate;
    private Long departmentId;

    public WarehouseFilterRequest(Long id) {
        this.id = id;
    }
}