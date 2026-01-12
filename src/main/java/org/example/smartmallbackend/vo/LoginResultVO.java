package org.example.smartmallbackend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "登录响应信息")
public class LoginResultVO implements Serializable {

    @Schema(description = "认证Token")
    private String token;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickName;

    // 根据需要决定是否返回手机号/邮箱，如果返回建议做脱敏处理
    // private String phone;
}