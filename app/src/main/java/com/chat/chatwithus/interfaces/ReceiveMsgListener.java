package com.chat.chatwithus.interfaces;

import com.chat.chatwithus.data.ChatMessage;



public interface ReceiveMsgListener {
    public boolean receive(ChatMessage msg);

}