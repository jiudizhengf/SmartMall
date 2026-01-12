package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@TableName("ums_user")
@Tag(name = "UmsUser", description = "用户实体")
public class UmsUser extends BaseEntity{
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String nickName;
}
