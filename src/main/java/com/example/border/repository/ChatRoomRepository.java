package com.example.border.repository;

import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.ChatRoom;
import com.example.border.model.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByEmployerAndApplicant(Employer employer, Applicant currentApplicant);

    List<ChatRoom> findChatsByApplicant_IdOrderByLastMessageTimestampDesc(UUID applicantId);

    List<ChatRoom> findChatsByEmployer_IdOrderByLastMessageTimestampDesc(UUID employerId);

    @Query(value =
            "SELECT cr FROM chat_room cr " +
            "JOIN chat_message cm ON cr.id = cm.chat_room_id " +
            "JOIN employer e ON cr.employer_id = e.id " +
            "WHERE cm.is_read = FALSE " +
            "AND cr.applicant_id = :applicantId " +
            "AND e.name LIKE :nameSearch",
            nativeQuery = true)
    List<ChatRoom> findUnreadChatsByApplicantIdAndEmployerName(
            @Param("applicantId") UUID applicantId,
            @Param("nameSearch") String nameSearch);

    @Query(value =
            "SELECT cr FROM chat_room cr " +
            "JOIN chat_message cm ON cr.id = cm.chat_room_id " +
            "JOIN applicant a ON cr.applicant_id = a.id " +
            "WHERE cm.is_read = FALSE " +
            "AND cr.employer_id = :employerId " +
            "AND (a.first_name LIKE :nameSearch OR a.last_name LIKE :nameSearch)",
            nativeQuery = true)
    List<ChatRoom> findUnreadChatByEmployerIdAndApplicantName(
            @Param("employerId") UUID employerId,
            @Param("nameSearch") String nameSearch);

    @Query(value =
            "SELECT count(cr.id) FROM chat_room cr " +
                    "JOIN chat_message cm ON cr.id = cm.chat_room_id " +
                    "JOIN employer e ON cr.employer_id = e.id " +
                    "JOIN users u ON e.user_id = u.id " +
                    "WHERE u.id = :userId " +
                    "AND cm.is_read = FALSE",
    nativeQuery = true)
    int findUnreadChatCount(@Param("userId") UUID userId);
}