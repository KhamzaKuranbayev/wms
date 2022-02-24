package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by norboboyev_h  on 07.09.2020  12:44
 */

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "inventarization")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Inventarization extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    @Column(name = "started_at")
    private Date startedAt;

//    @SortableField
//    @Field(analyze = Analyze.YES, indexNullAs = "null")
//    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column(name = "ended_at")
    private Date endedAt;

    @Column(name = "valids_count")
    private int validsCount;

    @Column(name = "invalids_count")
    private int invalidsCount;

    @IndexedEmbedded(depth = 0, includePaths = {"createdByUser.id", "creationDate"})
    @Embedded
    private _AuditInfo auditInfo;

    @IndexedEmbedded(depth = 0, includePaths = {"id2"})
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Warehouse warehouse;

    @NumericField
    @Field(name = "id", analyze = Analyze.NO, store = Store.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }
}
