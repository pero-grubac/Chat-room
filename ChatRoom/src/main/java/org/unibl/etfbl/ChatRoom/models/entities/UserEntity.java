package org.unibl.etfbl.ChatRoom.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class UserEntity implements UserDetails{
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "IdUser", nullable = false)
    private Integer idUser;
    @Basic
    @Column(name = "IsApproved", nullable = true)
    private Byte isApproved;
    @Basic
    @Column(name = "TwoFactorToken", nullable = true, length = 255, unique = true)
    private String twoFactorToken;
    @Basic
    @Column(name = "Username", nullable = false, length = 45, unique = true)
    private String username;
    @Basic
    @Column(name = "Email", nullable = false, length = 45)
    private String email;
    @Basic
    @Column(name = "Password", nullable = false, length = 255)
    private String password;
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<CommentEntity> comments;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<PermissionEntity> permissions;
    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = true)
    private RoleEnum role;
    @Enumerated(EnumType.STRING)
    @Column(name = "Source", nullable = true)
    private RegristrationSourceEnum source;
    @Basic
    @Column(name = "JWT", nullable = true)
    private String JWT;
    @ManyToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_has_room",
            joinColumns = @JoinColumn(name = "IdUser", referencedColumnName = "IdUser"),
            inverseJoinColumns = @JoinColumn(name = "IdForumRoom", referencedColumnName = "IdForumRoom")
    )
    private Set<ForumRoomEntity> rooms = new HashSet<>();
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        authorities.addAll(getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission().name()))
                .toList());
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
