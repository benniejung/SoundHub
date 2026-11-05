package com.yebin.sideproject.global.sms;

public interface SmsSender {

    void send(String to, String text);
}
