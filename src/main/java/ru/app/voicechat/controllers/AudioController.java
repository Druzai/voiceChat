package ru.app.voicechat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.app.voicechat.models.Recording;
import ru.app.voicechat.services.AudioService;
import ru.app.voicechat.services.UserService;

import java.util.Comparator;

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
                                !recording.get().getPrivateToOwner())))
        {
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
    public String deleteProduct(@RequestParam int id) {
        audioService.removeRecording(Integer.toUnsignedLong(id));
        return "redirect:/recordings";
    }
}
