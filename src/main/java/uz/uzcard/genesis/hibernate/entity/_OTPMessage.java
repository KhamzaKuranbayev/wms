package uz.uzcard.genesis.hibernate.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.OtpType;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert

@Table(name = "otp_message")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _OTPMessage extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(hidden = true)
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @org.hibernate.annotations.ForeignKey(name = "none")
    private _User user;
    @Column(insertable = false, updatable = false)
    private Long user_id;

    private String messageId;
    private String message;

    private Date createdDate;
    private Date updatedDate;
    private Date expireDate;
    private String code;
    private String uniqueParam;

    @Enumerated(EnumType.STRING)
    private OtpType type;

}