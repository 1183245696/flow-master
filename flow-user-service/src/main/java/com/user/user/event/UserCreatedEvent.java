package com.user.user.event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
@Data @AllArgsConstructor @NoArgsConstructor
public class UserCreatedEvent implements Serializable {
    private Long userId;
    private String username;
    private Long deptId;
    private Long positionId;
}
