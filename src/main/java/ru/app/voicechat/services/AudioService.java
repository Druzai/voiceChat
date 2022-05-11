package ru.app.voicechat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.app.voicechat.models.Recording;
import ru.app.voicechat.models.User;
import ru.app.voicechat.repositories.RecordingRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AudioService {
    @Autowired
    private RecordingRepository recordingRepository;

    @Transactional
    public List<Recording> getUserRecordings(User user){
        return recordingRepository.findByOwner(user);
    }

    @Transactional
    public List<Recording> getPublicRecordings(User user){
        return recordingRepository.findByPrivateToOwnerAndOwnerIsNot(false, user);
    }

    @Transactional
    public Optional<Recording> getRecording(Long id){
        return recordingRepository.findById(id);
    }

    @Transactional
    public Recording saveRecording(Recording recording){
        return recordingRepository.save(recording);
    }

    @Transactional
    public void removeRecording(Long id){
        recordingRepository.deleteById(id);
    }
}
