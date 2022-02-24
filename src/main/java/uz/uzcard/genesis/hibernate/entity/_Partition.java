package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.DoubleBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.dto.api.req.partition.PartitionCarriageAddressDto;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "partitions")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Partition extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Product product;

    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(name = "address", columnDefinition = "jsonb")
    private List<PartitionCarriageAddressDto> addresses = new ArrayList<>();

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ContractItem contractItem;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Warehouse warehouse;

    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double count;
    //    @NumericField
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = DoubleBridge.class)
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double remains;

    @SortableField
    @Field(analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    private LocalDate date;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    private Date expiration;


    @IndexedEmbedded(depth = 0, includePaths = {"creationDate"})
    @Embedded
    private _AuditInfo auditInfo;

    @IndexedEmbedded(depth = 1, includePaths = {"orderItem.itemNumb", "orderItem.parent.numb",
            "contractItem.numb", "contractItem.parent.code", "id",
            "orderItem.contractItem.parent.code"})
    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "partition")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state = 'NEW'")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_GivenProducts> givens = new ArrayList<>();

    @IndexedEmbedded(depth = 1, indexNullAs = "null")
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "partition")
    @OrderBy(value = "id")
    @Where(clause = "state = 'NEW'")
    private List<_Lot> lots = new ArrayList<>();

    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    @Override
    public Long getId() {
        return id;
    }

    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @Field(analyze = Analyze.NO)
    public String getId2() {
        return "" + getId();
    }

    public void setRemains(Double remains) {
        if (remains == null)
            remains = 0d;
        this.remains = remains;
    }

    public void setCount(Double count) {
        if (count == null)
            count = 0d;
        this.count = count;
    }

    @IndexedEmbedded(depth = 1, indexNullAs = "null", includePaths = {"parent.code",
            "orderItems.itemNumb", "orderItems.parent.numb", "orderItems.parent.id"}, includeEmbeddedObjectId = true)
    public _ContractItem getContractItem() {
        return getLazyColumn(contractItem);
    }

    @IndexedEmbedded(depth = 1)
    public _Product getProduct() {
        return getLazyColumn(product);
    }

    @IndexedEmbedded(depth = 1, includePaths = {"department.id"})
    public _Warehouse getWarehouse() {
        return getLazyColumn(warehouse);
    }
}