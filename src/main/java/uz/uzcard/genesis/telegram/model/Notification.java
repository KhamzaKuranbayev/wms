package uz.uzcard.genesis.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Javohir Elmurodov
 * @created 1/13/2021 | 5:22 PM
 * @project GTL
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Long id;
    private String column;
    private Integer row;
    private String name;
}
