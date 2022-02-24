package uz.uzcard.genesis.service;

public interface SmsService {
//    void check(_User user);

    void sendMessage(String phone, String text);
}
