package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.bridge.builtin.LongBridge;
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
@Table(name = "attachments")
public class _Attachment extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Basic(fetch = FetchType.LAZY)
    private byte[] data;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    @Field(analyze = Analyze.NO)
    @Column(unique = true)
    private String name;

    @Column(columnDefinition = "integer DEFAULT 0")
    private int pageSize;

    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb DEFAULT '[]'::jsonb", nullable = false)
    private List<String> pages = new ArrayList<>();

    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    @Override
    public Long getId() {
        return id;
    }
}