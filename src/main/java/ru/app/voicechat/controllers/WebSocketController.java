package ru.app.voicechat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.app.voicechat.components.WSMessage;
import ru.app.voicechat.models.User;
import ru.app.voicechat.services.AudioService;
import ru.app.voicechat.services.UserService;

@Controller
@Transactional
public class WebSocketController {
    @Autowired
    private AudioService audioService;

    @Autowired
    private UserService userService;

    @MessageMapping("/send")
    @SendTo("/topic/messages")
    @ResponseBody
    public WSMessage greeting(@RequestBody WSMessage message) {
        switch (message.getCommand()) {
            case "connect" -> {
                var room = audioService.getRoom(message.getRoomId());
                if (room.isEmpty())
                    throw new NullPointerException();
                var users = room.get().getUserList();
                users.add(userService.findById(message.getUserId()).get());
                room.get().setUserList(users);
                audioService.saveRoom(room.get());
                message.setUserList(users.stream().map(User::getUsername).toList());
            }
            case "disconnect" -> {
                var room = audioService.getRoom(message.getRoomId());
                if (room.isEmpty())
                    throw new NullPointerException();
                var users = room.get().getUserList();
                users.remove(userService.findById(message.getUserId()).get());
                room.get().setUserList(users);
                audioService.saveRoom(room.get());
                message.setUserList(users.stream().map(User::getUsername).toList());
            }
            case "voice" -> {
                var newContent = message.getContent().split(";");
                newContent[0] = "data:audio/ogg;";
                message.setContent(String.join("", newContent));
            }
        }
        return message;
    }
}
