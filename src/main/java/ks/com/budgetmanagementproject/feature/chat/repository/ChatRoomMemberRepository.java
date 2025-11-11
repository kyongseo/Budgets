package ks.com.budgetmanagementproject.feature.chat.repository;

import ks.com.budgetmanagementproject.feature.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    boolean existsByChatRoom_IdAndUserId(Long roomId, Long userId);
    Optional<ChatRoomMember> findByChatRoom_IdAndUserId(Long roomId, Long userId);
}