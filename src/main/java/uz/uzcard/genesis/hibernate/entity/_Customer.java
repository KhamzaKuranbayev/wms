package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.SortableField;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "customer")
public class _Customer extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @SortableField
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Column(name = "name")
    private String name;

    @SortableField
    @NumericField
    @Field(name = "id", analyze = Analyze.NO)
//    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }
}
