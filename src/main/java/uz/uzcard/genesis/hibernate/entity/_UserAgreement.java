package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.UserAgreementStatusType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "user_agreement")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _UserAgreement extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ContractItem contractItem;

    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _User user;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_agreement_files",
            joinColumns = {@JoinColumn(name = "user_agreement_id")},
            inverseJoinColumns = {@JoinColumn(name = "attachment_id")}
    )
    @Where(clause = "state != 'DELETED'")
    private List<_AttachmentView> resources = new ArrayList<>();

    /**
     * Reason
     */
    private String description;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(name = "is_read", columnDefinition = "boolean DEFAULT false")
    private boolean read;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(columnDefinition = "boolean DEFAULT false")
    private boolean arrived;

    //    @Field(analyze = Analyze.NO)
//    @FieldBridge(impl = BooleanBridge.class)
    @Column(columnDefinition = "boolean DEFAULT false")
    private boolean ozl;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(columnDefinition = "boolean DEFAULT false")
    private boolean issued;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(columnDefinition = "boolean DEFAULT false", name = "is_notification_sent")
    private boolean notificationSent;
    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column(name = "notificatio_sent_time")
    private Date notificationSentTime;

    @SortableField
    @Field(analyze = Analyze.NO)
    @Enumerated(EnumType.STRING)
    private UserAgreementStatusType statusType = UserAgreementStatusType.WAITING;

    @Column(name = "hash_esign", columnDefinition = "TEXT")
    private String hashESign;

    @IndexedEmbedded(depth = 1)
    @Embedded
    private _AuditInfo auditInfo;

    @NumericField
    @SortableField
    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    @Field(analyze = Analyze.NO, name = "initiatorInfo")
    public String getInitiatorInfo() {
        if (user == null)
            throw new ValidatorException("Инициатор бириктирилмаган");
        if (statusType == null)
            throw new ValidatorException("Статус номаълум");
        return String.format("%s_%s_%s", user.getId(), notificationSent, statusType);
    }

    @IndexedEmbedded(depth = 2, includePaths = {"parent.id"})
    public _ContractItem getContractItem() {
        return getLazyColumn(contractItem);
    }
}