/*
package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "report_views")
public class _ReportView extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    private String accountingCode;

    private Long productId;
    private String productName;

    private Long productTypeId;
    private String productTypeName;

    private Date creationDate;
    private Date updatedDate;

    private Double acceptedCount;
    private Double acceptedPrice;

    private Double givenCount;
    private Double givenPrice;

    private Double remainCount;
    private Double remainPrice;

    private String unitTypeRu;
    private String unitTypeEn;
    private String unitTypeUz;
    private String unitTypeCyrl;
}
*/
