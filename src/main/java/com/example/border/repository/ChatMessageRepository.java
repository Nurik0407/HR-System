package com.example.border.repository;

import com.example.border.model.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query(value =
            "SELECT count(*) FROM chat_message c " +
            "WHERE c.is_read=FALSE " +
            "AND c.chat_room_id = :chatRoomId"
            ,nativeQuery = true)
    int getUnreadMessagesCount(UUID chatRoomId);

    List<ChatMessage> findChatMessageByChatRoom_IdOrderByTimestampDesc(UUID chatRoomId);
}