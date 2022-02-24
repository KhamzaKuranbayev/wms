package uz.uzcard.genesis.hibernate.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Created by Virus on 31-Aug-16.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class _Item extends _Entity {

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String name_ru;
    @Column(nullable = false)
    private String name_en;
    @Column(nullable = false)
    private String name_uzl;

    @Override
    public String toString() {
        return GlobalizationExtentions.getName(this);
    }

    @Transient
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public SelectItem selectItem() {
        return new SelectItem(getId(), name, "" + getId());
    }

    public void setString(String name) {
        GlobalizationExtentions.setName(this, name);
    }

    public String getNameCyrl() {
        return name;
    }
}