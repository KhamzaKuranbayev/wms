package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base.StringCollectionFieldBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "attributes")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Attribute extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.YES)
//    @Column(unique = true)
    private String name;

    @IndexedEmbedded(depth = 0, includePaths = {"creationDate"})
    @Embedded
    private _AuditInfo auditInfo;

//    @Basic(fetch = FetchType.LAZY)
//    @ElementCollection
//    @CollectionTable(name = "_attribute_items", joinColumns = @JoinColumn(name = "_attribute_id"))
//    @Column(name = "items")
//    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    private List<String> items2 = new ArrayList<>();

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = StringCollectionFieldBridge.class)
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb DEFAULT '[]'::jsonb", nullable = false)
    private List<String> items = new ArrayList<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    private boolean byDefault;

    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    @Override
    public Long getId() {
        return id;
    }
}
