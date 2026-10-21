package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;

import java.util.List;

/**
 * The signed-in user as the web app sees it. The JWT itself only travels in the httpOnly
 * session cookie, so page scripts never get to read it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String role;
    private List<String> permissions;
    /** Rooms the user belongs to; a moderator moderates only these. */
    private List<Integer> rooms;

    public static AuthResponse of(UserEntity user, List<Integer> rooms) {
        return AuthResponse.builder()
                .username(user.getUsername())
                .role(user.getRole() == null ? null : user.getRole().name())
                .permissions(user.getPermissions() == null ? List.of() : user.getPermissions().stream()
                        .map(permission -> permission.getPermission().name())
                        .toList())
                .rooms(rooms)
                .build();
    }
}
