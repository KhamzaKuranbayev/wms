package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.AttachmentDao;
import uz.uzcard.genesis.hibernate.entity._Attachment;
import uz.uzcard.genesis.hibernate.entity._AttachmentView;
import uz.uzcard.genesis.hibernate.entity._State;

@Component(value = "attachmentDao")
public class AttachmentDaoImpl extends DaoImpl<_Attachment> implements AttachmentDao {
    public AttachmentDaoImpl() {
        super(_Attachment.class);
    }

    @Override
    public _Attachment getByName(String name) {
        return (_Attachment) findSingle("select t from _Attachment t where t.state != :deleted and t.name = :name",
                preparing(new Entry("deleted", _State.DELETED), new Entry("name", name)), Constants.Cache.QUERY_ATTACHMENT);
    }

    @Override
    public _AttachmentView getById(Long id) {
        return (_AttachmentView) findSingle("select t from _AttachmentView t where t.id = :id",
                preparing(new Entry("id", id)));
    }
}