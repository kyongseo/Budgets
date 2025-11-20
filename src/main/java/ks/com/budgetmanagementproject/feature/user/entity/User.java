package ks.com.budgetmanagementproject.feature.user.entity;

import jakarta.persistence.*;
import ks.com.budgetmanagementproject.feature.role.entity.Role;
import ks.com.budgetmanagementproject.global.common.model.BaseTimeEntity;
import lombok.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 50)
    private String phoneNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.roles = Collections.singleton(role);
    }

    public void updateUserInfo(String nickname, String phoneNumber) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }
}
