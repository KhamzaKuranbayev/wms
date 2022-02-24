package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Attachment;
import uz.uzcard.genesis.hibernate.entity._AttachmentView;

public interface AttachmentDao extends Dao<_Attachment> {
    _Attachment getByName(String name);

    _AttachmentView getById(Long id);
}