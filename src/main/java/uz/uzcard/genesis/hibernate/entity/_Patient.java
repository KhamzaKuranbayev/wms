package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.Date;

@Indexed
@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@Table(name = "patients")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
public class _Patient extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.YES)
    private String fio;

    @Field(analyze = Analyze.YES)
    private String pasNumber;

    @Field(analyze = Analyze.YES)
    private String diagnosis;

    @Field(analyze = Analyze.YES)
    private String conclusion;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    private Date birthday;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    private Date lastTakenAwayDate;

    @Embedded
    private _AuditInfo auditInfo;

    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    @Override
    public Long getId() {
        return id;
    }
}
