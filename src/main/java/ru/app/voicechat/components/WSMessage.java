package ru.app.voicechat.components;

import lombok.Data;

import java.util.List;

@Data
public class WSMessage {
    private Long userId;
    private String userName;
    private Long roomId;
    private String command;
    private String content;
    private List<String> userList;
}

