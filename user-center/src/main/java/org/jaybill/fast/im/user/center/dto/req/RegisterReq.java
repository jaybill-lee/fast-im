package org.jaybill.fast.im.user.center.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {
    private String nickname;
    private String password;
    private String confirmPassword;
    private String captchaId;
    private String captchaText;
}
