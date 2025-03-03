package com.example.border.repository;

import com.example.border.model.entity.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query(value =
            "SELECT count(n) FROM notification n " +
                    "JOIN employer e ON n.employer_id = e.id " +
                    "WHERE e.id = :employerId " +
                    "AND n.is_read = FALSE",
            nativeQuery = true)
    int getCountUnreadNotificationsByEmployerId(@Param("employerId") UUID id);

    @Query(value =
            "SELECT count(n) FROM notification n " +
                    "JOIN applicant a ON n.applicant_id = a.id " +
                    "WHERE a.id = :applicantId " +
                    "AND n.is_read = FALSE",
            nativeQuery = true)
    int getCountUnreadNotificationsByApplicantId(@Param("applicantId") UUID id);

    List<Notification> findByEmployer_Id(UUID employerId, Sort sort);

    List<Notification> findByApplicant_Id(UUID applicantId, Sort sort);
}