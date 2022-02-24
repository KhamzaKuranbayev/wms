package uz.uzcard.genesis.uitls;

import uz.uzcard.genesis.hibernate.entity._State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateMap {
    private static final List<List<String>> statusMap = new ArrayList<>();

    static {
        //order
        statusMap.add(Arrays.asList(_State.NEW, _State.TO_REVIEW_WAREHOUSE));
        statusMap.add(Arrays.asList(_State.OZL_TO_DEPARTMENT, _State.PAPER_EXPECTED_SPECIFICATION));
        statusMap.add(Arrays.asList(_State.TO_REVIEW_WAREHOUSE, _State.REJECT_ORDER_ITEM));
        statusMap.add(Arrays.asList(_State.TO_REVIEW_WAREHOUSE, _State.PAPER_EXPECTED_SPECIFICATION));
        statusMap.add(Arrays.asList(_State.TO_REVIEW_WAREHOUSE, _State.YES_PRODUCT));
        statusMap.add(Arrays.asList(_State.PAPER_EXPECTED_SPECIFICATION, _State.PENDING_PURCHASE));
        statusMap.add(Arrays.asList(_State.PENDING_PURCHASE, _State.APPLICATION_REJECTED_OZL));
        statusMap.add(Arrays.asList(_State.PENDING_PURCHASE, _State.DELIVERY_EXPECTED));
        statusMap.add(Arrays.asList(_State.PENDING_PURCHASE, _State.READY_TO_PRODUCE));
        statusMap.add(Arrays.asList(_State.PENDING_PURCHASE, _State.OZL_TO_DEPARTMENT));
        statusMap.add(Arrays.asList(_State.YES_PRODUCT, _State.READY_TO_PRODUCE));
        statusMap.add(Arrays.asList(_State.DELIVERY_EXPECTED, _State.READY_TO_PRODUCE));
        statusMap.add(Arrays.asList(_State.DELIVERY_EXPECTED, _State.PENDING_PURCHASE));
        statusMap.add(Arrays.asList(_State.DELIVERY_EXPECTED, _State.YES_PRODUCT));
        statusMap.add(Arrays.asList(_State.DELIVERY_EXPECTED, _State.ISSUED_ORDER_ITEM));
        statusMap.add(Arrays.asList(_State.ISSUED_ORDER_ITEM, _State.YES_PRODUCT));
        statusMap.add(Arrays.asList(_State.READY_TO_PRODUCE, _State.RECEIVED));
        statusMap.add(Arrays.asList(_State.TO_REVIEW_WAREHOUSE, _State.READY_TO_PRODUCE));
        //contract
        statusMap.add(Arrays.asList(_State.CONTRACT_CONCLUTION, _State.CONTRACT_REJECT));
        statusMap.add(Arrays.asList(_State.CONTRACT_CONCLUTION, _State.CONTRACT_ACCEPTED));
        //contract item
        statusMap.add(Arrays.asList(_State.NEW, _State.CONTRACT_ITEM_ACCEPTED));
        statusMap.add(Arrays.asList(_State.NEW, _State.CONTRACT_ITEM_PART_ACCEPTED));
        statusMap.add(Arrays.asList(_State.NEW, _State.CONTRACT_ITEM_PARTITION_ACCEPTED));
        statusMap.add(Arrays.asList(_State.CONTRACT_ITEM_PART_ACCEPTED, _State.CONTRACT_ITEM_REJECT));
        statusMap.add(Arrays.asList(_State.CONTRACT_ITEM_PARTITION_ACCEPTED, _State.CONTRACT_ITEM_ACCEPTED));
        statusMap.add(Arrays.asList(_State.CONTRACT_ITEM_PARTITION_ACCEPTED, _State.CONTRACT_ITEM_PARTITION_ACCEPTED));
        statusMap.add(Arrays.asList(_State.NEW, _State.CONTRACT_ITEM_REJECT));
    }

    public static boolean match(String oldState, String newState) {
        return statusMap.contains(Arrays.asList(oldState, newState));
    }
}