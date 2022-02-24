package uz.uzcard.genesis.config;

import java.io.Serializable;

public interface Constants extends Serializable {

    String ACCESS_TOKEN = "access_token";

    //Role begin
    String ADMIN = "ADMIN";
    //Role end

    //OtpMessage type
    String SMS = "SMS";
    String Email = "Email";
    //OtpMessage type

    interface Cache {
        String QUERY_USER = "query.user";
        String QUERY_ROLE = "query.role";
        String QUERY_CLOSESTATUS = "query.close_status";
        String QUERY_DISABLESTATUS = "query.disable_status";
        String QUERY_WARNSTATUS = "query.warn_status";
        String QUERY_MISCSTATUS = "query.misc_status";
        String QUERY_STATE = "query.state";
        String QUERY_ORDER = "query.order";
        String QUERY_ORDER_ITEM = "query.order_item";
        String QUERY_STILLAGE = "query.stillage";
        String QUERY_CONTRACT_ITEM = "query.contract_item";
        String QUERY_CONTRACT = "query.contract";
        String QUERY_TEAM = "query.team";
        String QUERY_PRODUCT = "query.product";
        String QUERY_DEPARTMENT = "query.department";
        String QUERY_ATTACHMENT = "query.attachment";
    }

}
