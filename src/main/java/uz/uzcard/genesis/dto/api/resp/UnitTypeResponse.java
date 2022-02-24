package uz.uzcard.genesis.dto.api.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * created by Madaminov Javohir on 16.10.2020
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnitTypeResponse {
    private Long id;
    private String name_en;
    private String name_uz;
    private String name_ru;
    private String name_cyrl;
}