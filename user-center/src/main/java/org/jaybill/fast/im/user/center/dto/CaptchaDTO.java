package org.jaybill.fast.im.user.center.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CaptchaDTO {
    private String id;
    private String imageBase64;
}
