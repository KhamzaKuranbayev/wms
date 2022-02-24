package uz.uzcard.genesis.hibernate.enums;

/**
 * Created by norboboyev_h  on 24.12.2020  10:58
 */

public enum NotificationCategoryType {
    /**
     * Inisiator qo'shilganda inisiatorlarga jo'natish
     */
    INITIATOR_ADDED_SENT_TO_INITIATORS,

    /**
     * Inisiator qo'shilganda inisiatorlarni departmentlariga jo'natish
     */
    INITIATOR_ADDED_SENT_BY_DEP_USERS,

    /**
     * Produkt berilganda zakaz bergan userni xabardor qilish
     */
    PRODUCT_PRODUCED_SENT_TO_DEP_USER,

    /**
     * Kontrakt bo'yicha mahsulot kelganda omtkni xabardor qilish
     */
    PRODUCT_ARRIVED_SENT_TO_OMTK,

    /**
     * Inisiator OMTK dan mahsulotni qisman olib ketishni so'raganda
     */
    REQUEST_FOR_PRODUCING_SENT_TO_OMTK
}
