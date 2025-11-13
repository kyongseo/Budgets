package ks.com.budgetmanagementproject.feature.chat.entity;

import jakarta.persistence.*;
import ks.com.budgetmanagementproject.global.common.model.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_rooms")
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String creatorName;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ChatRoomMember> members = new HashSet<>();

    public void addMember(ChatRoomMember member) {
        this.members.add(member);
    }

    public void removeMember(ChatRoomMember member) {
        this.members.remove(member);
    }

    public int getMemberCount() {
        return this.members.size();
    }

    public boolean hasMember(Long userId) {
        return this.members.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }
}
