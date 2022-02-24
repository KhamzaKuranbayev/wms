package uz.uzcard.genesis.hibernate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.StringBridge;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.uitls.StateConstants;

import javax.persistence.*;


@Indexed
@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "state")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _State extends _Entity implements StateConstants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.NO)
    @Column(unique = true)
    private String code;
    private String name;
    private String description;
    private String colour;

    @SortableField
    @NumericField
    @Field(analyze = Analyze.NO)
    private int sortOrder;

    @Field(analyze = Analyze.NO, store = Store.NO, indexNullAs = "null")
    @FieldBridge(impl = StringBridge.class)
    private String entityName;

    @Field(analyze = Analyze.NO, name = "id")
    public Long getId() {
        return id;
    }
}