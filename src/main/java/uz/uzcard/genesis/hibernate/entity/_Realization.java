package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.*;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.Date;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "realization")
public class _Realization extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @SortableField
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @SortableField
    @Field(analyze = Analyze.YES)
    @Column(name = "contract_number")
    private String contractNumber;

    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Product product;

    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Department department;

    @SortableField
    @NumericField
    @Field(analyze = Analyze.NO, store = Store.NO)
//    @FieldBridge(impl = DoubleBridge.class)
    @Column(name = "given_count")
    private Double count;

    @SortableField
    @NumericField
    @Field(analyze = Analyze.NO, store = Store.NO)
//    @FieldBridge(impl = DoubleBridge.class)
    @Column(name = "contract_balance")
    private Double contractBalance;

    @SortableField
    @Field(analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    private Date realizationDate;

    /*
        Ед.изм
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _UnitType unitType;

    @IndexedEmbedded(depth = 1)
    @Embedded
    private _AuditInfo auditInfo;

    @NumericField
    @SortableField
    @Field(name = "id", analyze = Analyze.NO)
//    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }
}
