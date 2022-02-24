package uz.uzcard.genesis.uitls;

public interface StateConstants {
    String NEW = "NEW";
    String DELETED = "DELETED";
    // Product status in Warehouse
    String USED = "USED";

    //Order status
    String NOT_ACCEPTED_ORDER = "NOT_ACCEPTED_ORDER";
    /*
    Ozl to Department
     */
    String OZL_TO_DEPARTMENT = "OZL_TO_DEPARTMENT";
    String ACCEPTED_ORDER = "ACCEPTED_ORDER";
    //Order Item status
    /*
    На рассмотрении ОМТК
     */
    String TO_REVIEW_WAREHOUSE = "TO_REVIEW_WAREHOUSE";
    /*
    TMЦ нет на складе
     */
//    String NO_PRODUCT = "NO_PRODUCT";
    /*
    TMЦ на складе
     */
    String YES_PRODUCT = "YES_PRODUCT";
    /*
      Получено
     */
    String RECEIVED = "RECEIVED";
    /*
    Готов к выдаче
     */
    String READY_TO_PRODUCE = "READY_TO_PRODUCE";
    /*
    Заявка отклонена ОМТК
     */
    String REJECT_ORDER_ITEM = "REJECT_ORDER_ITEM";
    /*
    Ожидается бумажная спецификация
     */
    String PAPER_EXPECTED_SPECIFICATION = "PAPER_EXPECTED_SPECIFICATION";
    /*
    Ожидается закупка
     */
    String PENDING_PURCHASE = "PENDING_PURCHASE";
    /*
    Заявка отклонена ОЗЛ
     */
    String APPLICATION_REJECTED_OZL = "APPLICATION_REJECTED_OZL";
    /*
    Ожидается поставка
     */
    String DELIVERY_EXPECTED = "DELIVERY_EXPECTED";
    /*
    Принял ОМТК
     */
    String ISSUED_ORDER_ITEM = "ISSUED_ORDER_ITEM";
    /*
    Заключен контракт
     */
    String CONTRACT_CONCLUTION = "CONTRACT_CONCLUTION";
    /*
    Контракт расторгнут
     */
    String CONTRACT_REJECT = "CONTRACT_REJECT";
    /*
    Удовлетворен
     */
    String PRODUCT_DELIVERED = "PRODUCT_DELIVERED";
    //Otp status
    String CONFIRMED = "CONFIRMED";
    // Contract status
//    String CONTRACT_NEW = "CONTRACT_NEW";

    /**
     * Контракт принят
     */
    String CONTRACT_ACCEPTED = "CONTRACT_ACCEPTED";

    //    String CONTRACT_REJECT = "CONTRACT_REJECT";
//    String PART_ACCEPTED = "PART_ACCEPTED";
    // Contract status
    // Out Going Contract status
    String CONCLUDED = "CONCLUDED";
    String COMPLETED = "COMPLETED";

    /**
     * Contract Item status
     */
    String CONTRACT_ITEM_ACCEPTED = "CONTRACT_ITEM_ACCEPTED";
    String CONTRACT_ITEM_PART_ACCEPTED = "CONTRACT_ITEM_PART_ACCEPTED";
    String CONTRACT_ITEM_PARTITION_ACCEPTED = "CONTRACT_ITEM_PARTITION_ACCEPTED";
    String CONTRACT_ITEM_REJECT = "CONTRACT_ITEM_REJECT";

    /**
     * GIVEN PRODUCTS ARE TAKEN
     */
    String PRODUCT_ACCEPTED = "PRODUCT_ACCEPTED";

    /**
     * PRODUCT ITEMS
     */
    String PRODUCT_PRODUCED = "PRODUCT_PRODUCED";
    /**
     * PRODUCT ITEMS
     */
    String PRODUCT_USED = "PRODUCT_USED";
    /**
     * PRODUCT ITEMS
     */
    String PRODUCT_INVALID = "PRODUCT_INVALID";

    /**
     * Socket Event Types
     * _Order
     */
    String ORDER_CREATE_EVENT = "ORDER_CREATE_EVENT";
    String ORDER_SEND_EVENT = "ORDER_SEND_EVENT";

    /**
     * _OrderItem
     */
    String ORDER_ITEM_CREATE_EVENT = "ORDER_ITEM_CREATE_EVENT";
    String ORDER_ITEM_CREATE_OMTK_TENDER_EVENT = "ORDER_ITEM_CREATE_OMTK_TENDER_EVENT";
    String ORDER_ITEM_UPDATE_EVENT = "ORDER_ITEM_UPDATE_EVENT";
    String ORDER_ITEM_UPDATE_COUNT_EVENT = "ORDER_ITEM_UPDATE_COUNT_EVENT";
    String ORDER_ITEM_DELETE_EVENT = "ORDER_ITEM_DELETE_EVENT";
    String ORDER_ITEM_CHANGE_STATUS_EVENT = "ORDER_ITEM_CHANGE_STATUS_EVENT";
    String ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT = "ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT";

    /**
     * _Contract
     */
    String CONTRACT_CREATE_EVENT = "CONTRACT_CREATE_EVENT";
    String CONTRACT_UPDATE_EVENT = "CONTRACT_UPDATE_EVENT";
    String CONTRACT_ACCEPTED_EVENT = "CONTRACT_ACCEPTED_EVENT";
    String CONTRACT_REJECTED_EVENT = "CONTRACT_REJECTED_EVENT";

    /**
     * _Producing_history
     */
    String REQUEST_DONE = "REQUEST_DONE";


    /**
     * _Rent
     */
    String RETURNED = "RETURNED";

    /**
     * _HistoryOfMedicineTaken // ON_HOLD
     */
    String ON_HOLD = "ON_HOLD";

    /**
     * _HistoryOfMedicineTaken // TAKEN_AWAY
     */
    String TAKEN_AWAY = "TAKEN_AWAY";
}