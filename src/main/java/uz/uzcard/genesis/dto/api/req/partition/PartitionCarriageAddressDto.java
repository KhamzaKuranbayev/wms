package uz.uzcard.genesis.dto.api.req.partition;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 29.10.2020  12:12
 */
@Getter
@Setter
public class PartitionCarriageAddressDto implements Serializable {
    private String addresse;
    private Long stillage;
    private String stillageName;
    private Long stillageColumn;
    private String stillageColumnName;
    private Long carriage;
    private Integer carriagePosition;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return this.toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        return "PartitionCarriageAddressDto{" +
                "stillage='" + stillage + '\'' +
                ", stillageColumn='" + stillageColumn + '\'' +
                ", carriage='" + carriage + '\'' +
                '}';
    }
}