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
import org.hibernate.search.bridge.builtin.IntegerBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "warehouses")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Warehouse extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Fields(value = {@Field(analyze = Analyze.YES),
            @Field(analyze = Analyze.NO, name = "nameSortEn", index = Index.NO)})
    @SortableField(forField = "nameSortEn")
    private String nameEn;

    @Fields(value = {@Field(analyze = Analyze.YES),
            @Field(analyze = Analyze.NO, name = "nameSortUz", index = Index.NO)})
    @SortableField(forField = "nameSortUz")
    private String nameUz;

    @Fields(value = {@Field(analyze = Analyze.YES),
            @Field(analyze = Analyze.NO, name = "nameSortRu", index = Index.NO)})
    @SortableField(forField = "nameSortRu")
    private String nameRu;

    private String address;

    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "warehouse")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    private List<_Stillage> stillages = new ArrayList<>();

    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "warehouse")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    @OrderBy(value = "sort_order")
    private List<_WarehouseX> columns = new ArrayList<>();

    @IndexedEmbedded(depth = 0, includePaths = {"id", "nameUz", "nameRu", "nameEn"})
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Department department;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = IntegerBridge.class)
    private Integer occupancyPercent;
    @Embedded
    private _AuditInfo auditInfo;

    @Field(analyze = Analyze.NO, name = "idSort", index = Index.NO)
    @NumericField(forField = "idSort")
    @SortableField(forField = "idSort")
    @Override
    public Long getId() {
        return id;
    }

    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @Field(analyze = Analyze.NO)
    public String getId2() {
        return "" + getId();
    }

    @Field(analyze = Analyze.NO, index = Index.NO, name = "percentSort")
    @NumericField(forField = "percentSort")
    @SortableField(forField = "percentSort")
    public Integer getOccupancyPercent() {
        return occupancyPercent;
    }

    public String getNameByLanguage() {
        String language = SessionUtils.getInstance().getLanguage();
        switch (language) {
            case "en":
                return nameEn;
            case "ru":
                return nameRu;
            case "uz":
                return nameUz;
            default:
                return nameUz;
        }
    }
}