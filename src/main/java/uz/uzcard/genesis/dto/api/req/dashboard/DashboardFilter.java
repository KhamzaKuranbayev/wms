package uz.uzcard.genesis.dto.api.req.dashboard;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DashboardFilter extends FilterBase {
    private DateType dateType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date toDate;
    private ResolutionType resolutionType = ResolutionType.DAY;
    private Long departmentId;

    @ApiModelProperty(hidden = true)
    public Date[] getPeriod() {
        if (dateType == null)
            return null;
        Date date = new Date();
        switch (dateType) {
            case Day:
                return new Date[]{new Date(date.getYear(), date.getMonth(), date.getDate()), date};
            case Date:
                return new Date[]{fromDate, toDate};
            case Month:
                return new Date[]{new Date(date.getYear(), date.getMonth(), 1), date};
            case Year:
                return new Date[]{new Date(new Date().getYear(), 0, 1), new Date()};
            case Quarter:
                return new Date[]{new Date(date.getYear(), date.getMonth() / 3 * 3, 1), new Date()};
            default:
                return null;
        }
    }

    public enum DateType {
        Date,
        Day,
        Month,
        Quarter,
        Year
    }

    public enum ResolutionType {
        DAY,
        MONTH
    }
}
