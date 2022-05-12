package ru.app.voicechat.components;

import lombok.Data;

import java.util.List;

@Data
public class WSMessage {
    private Long userId;
    private Long roomId;
    private String command;
    private String audioContent;
    private List<String> userList;
}

