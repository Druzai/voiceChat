package ru.app.voicechat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.app.voicechat.models.Recording;
import ru.app.voicechat.models.Room;
import ru.app.voicechat.models.User;
import ru.app.voicechat.services.AudioService;
import ru.app.voicechat.services.UserService;

import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class AudioController {
    @Autowired
    private UserService userService;

    @Autowired
    private AudioService audioService;

    @GetMapping("/recordings")
    public String getRecordings(Model model) {
        var user = userService.getUser();
        var recordings = audioService.getUserRecordings(user);
        recordings.addAll(audioService.getPublicRecordings(user));
        recordings.sort(Comparator.comparing(Recording::getId));
        model.addAttribute("recordings", recordings);
        return "recordings";
    }

    @GetMapping("/rec/{id}")
    public String getDocument(@PathVariable int id, Model model) {
        var recording = audioService.getRecording(Integer.toUnsignedLong(id));
        if (recording.isPresent() &&
                (recording.get().getOwner().equals(userService.getUser()) ||
                        (!recording.get().getOwner().equals(userService.getUser()) &&
                                !recording.get().getPrivateToOwner()))) {
            model.addAttribute("recording", recording.get());
            return "curr_recording";
        }
        else if (recording.isPresent()) {
            model.addAttribute("reason", "Это приватная запись с id " + id);
            return "error";
        } else {
            model.addAttribute("reason", "Не найдена запись с id " + id);
            return "error";
        }
    }

    @GetMapping("/rec/new")
    public String getNewRecording(Model model) {
        model.addAttribute("new_recording", new Recording());
        return "new_recording";
    }

    @PostMapping("/rec/new")
    public String setNewRecording(@ModelAttribute("new_recording") Recording new_recording) {
        new_recording.setOwner(userService.getUser());
        var recording = audioService.saveRecording(new_recording);
        return "redirect:/rec/" + recording.getId();
    }

    @PostMapping("/rec/delete")
    public String deleteProduct(@RequestParam int id, Model model) {
        var recording = audioService.getRecording(Integer.toUnsignedLong(id));
        if (recording.isPresent())
            if (recording.get().getOwner().equals(userService.getUser())) {
                audioService.removeRecording(Integer.toUnsignedLong(id));
                return "redirect:/recordings";
            } else {
                model.addAttribute("reason", "Это запись с id " + id + " принадлежит не вам!");
                return "error";
            }
        else {
            model.addAttribute("reason", "Не найдена запись с id " + id);
            return "error";
        }
    }

    @GetMapping("/voicechats")
    public String getVoiceChats(Model model) {
        model.addAttribute("voice_chats", audioService.getRooms());
        return "voice_chats";
    }

    @GetMapping("/vc/new")
    public String getNewVC(Model model) {
        model.addAttribute("new_voice_chat", new Room());
        return "new_voice_chat";
    }

    @PostMapping("/vc/new")
    public String setNewVC(@ModelAttribute("voice_chat") Room new_room) {
        audioService.saveRoom(new_room);
        return "redirect:/voicechats";
    }

    @GetMapping("/vc/{id}")
    public String getVC(@PathVariable int id, Model model) {
        var room = audioService.getRoom(Integer.toUnsignedLong(id));
        if (room.isPresent()) {
            model.addAttribute("room", room.get());
            model.addAttribute("userList",
                    room.get().getUserList().stream().map(User::getUsername).collect(Collectors.joining(", ")));
            model.addAttribute("user", userService.getUser());
            return "curr_voice_chat";
        } else {
            model.addAttribute("reason", "Не найден чат с id " + id);
            return "error";
        }
    }
}
