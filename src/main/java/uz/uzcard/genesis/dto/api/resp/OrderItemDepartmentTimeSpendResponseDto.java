package uz.uzcard.genesis.dto.api.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDepartmentTimeSpendResponseDto implements Serializable {
    private String departmentName;
    private String lastName;
    private String firstName;
    private Long time;
}