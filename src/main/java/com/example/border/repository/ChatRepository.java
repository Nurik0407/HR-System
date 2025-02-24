package com.example.border.repository;

import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.ChatRoom;
import com.example.border.model.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatRoom, UUID> {
  Optional<ChatRoom> findByEmployerAndApplicant(Employer employer, Applicant currentApplicant);

  List<ChatRoom> findChatsByApplicant_IdOrderByLastMessageTimestampDesc(UUID applicantId);

  List<ChatRoom> findChatsByEmployer_IdOrderByLastMessageTimestampDesc(UUID employerId);
}