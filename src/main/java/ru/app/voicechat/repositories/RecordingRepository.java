package ru.app.voicechat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.app.voicechat.models.Recording;
import ru.app.voicechat.models.User;

import java.util.List;

@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording> findByOwner(User owner);

    List<Recording> findByPrivateToOwnerAndOwnerIsNot(Boolean privateToOwner, User owner);
}
