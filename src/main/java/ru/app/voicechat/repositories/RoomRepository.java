package ru.app.voicechat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.app.voicechat.models.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
