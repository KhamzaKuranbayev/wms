package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.IntegerBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base.StringCollectionFieldBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.Entity;
import javax.persistence.Table;
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
@Table(name = "orders")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Order extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO, store = Store.NO)
    @SortableField
    private String state = "NEW";

    @IndexedEmbedded(depth = 0, includePaths = {"createdByUser.id", "creationDate"})
    @Embedded
    private _AuditInfo auditInfo;

    @Field(name = "sendDate", index = Index.YES, analyze = Analyze.YES, indexNullAs = "null")
    @SortableField(forField = "sendDate")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    private Date sendDate;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column(name = "time_to_be_entered")
    private Date timeToBeEntered;

    @Fields(value = {@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = IntegerBridge.class)),
            @Field(analyze = Analyze.NO, name = "orderNumbSort", index = Index.NO)})
    @NumericField(forField = "orderNumbSort")
    @SortableField(forField = "orderNumbSort")
    @Column(columnDefinition = "integer DEFAULT 1")
    private int numb = 1;

    @IndexedEmbedded(depth = 0, includePaths = {"auditInfo.createdByUser.id",
            "department.id",
            "contractItem.parent.code",
            "contractItem.parent.supplier.id",
            "statusChangedUser.firstName",
            "productGroup.id", "productType.id", "product.name", "state", "statusChangedUser.id"})
    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_OrderItem> items = new ArrayList<>();

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToMany(fetch = FetchType.LAZY)
    @Where(clause = "state != 'DELETED'")
    private List<_AttachmentView> attachments = new ArrayList<>();

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"id"})
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _User statusChangedUser;

    @IndexedEmbedded(depth = 0, includePaths = {"id", "parent.id"})
    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _Department department;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = StringCollectionFieldBridge.class)
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private List<Long> teams = new ArrayList<>();

    private Integer defaultYearly;

    @SortableField
    @NumericField
    @Field(name = "id", analyze = Analyze.NO, store = Store.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    @SortableField(forField = "updateUser")
    @Field(index = Index.NO, name = "updateUser", indexNullAs = "null")
    public String getUpdateUser() {
        return statusChangedUser == null ? null : statusChangedUser.getShortName();
    }

    @Facet(name = "sendDateFacetDay")
    @Field(name = "sendDateFacetDay", analyze = Analyze.NO)
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    public Date getSendDateFacetDay() {
        return sendDate;
    }

    @Facet(name = "sendDateFacetMonth")
    @Field(name = "sendDateFacetMonth", analyze = Analyze.NO)
    @DateBridge(resolution = Resolution.MONTH, encoding = EncodingType.STRING)
    public Date getSendDateFacetMonth() {
        return sendDate;
    }
}