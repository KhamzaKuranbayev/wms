package uz.uzcard.genesis.hibernate.base;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.search.annotations.*;
import uz.uzcard.genesis.hibernate.entity._User;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Embeddable
public class _AuditInfo implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    @SortableField
    @Field(analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column(name = "creation_date")
    private Date creationDate = new Date();
    @SortableField
    @Field(analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column(name = "updated_date")
    private Date updatedDate;

    @IndexedEmbedded(includeEmbeddedObjectId = true, depth = 0, includePaths = {"userName", "userNameSort", "phone", "email", "shortName", "id", "id2", "state"})
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _User createdByUser;
    @Column(insertable = false, updatable = false, name = "created_by_user_id")
    private Long created_by_user_id;

    @IndexedEmbedded(includeEmbeddedObjectId = true, depth = 0, includePaths = {"userName", "userNameSort", "phone", "email", "shortName", "id", "id2", "state"})
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _User updatedByUser;
    @Column(insertable = false, updatable = false, name = "updated_by_user_id")
    private Long updated_by_user_id;

    public _AuditInfo(Date creationDate, Date updatedDate, _User createdByUser, _User updatedByUser) {
        this.creationDate = creationDate;
        this.updatedDate = updatedDate;
        this.createdByUser = createdByUser;
        this.updatedByUser = updatedByUser;
    }

    public _AuditInfo() {
    }

    @Field(name = "createdByUserId", analyze = Analyze.NO)
    public Long getCreatedByUserId() {
        return created_by_user_id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public _User getCreatedByUser() {
        return _Entity.getLazyColumn(createdByUser);
    }

    public void setCreatedByUser(_User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public _User getUpdatedByUser() {
        return _Entity.getLazyColumn(updatedByUser);
    }

    public void setUpdatedByUser(_User updatedByUser) {
        this.updatedByUser = updatedByUser;
    }

    public Long getUpdated_by_user_id() {
        return updated_by_user_id;
    }

    public Long getCreated_by_user_id() {
        return created_by_user_id;
    }
}