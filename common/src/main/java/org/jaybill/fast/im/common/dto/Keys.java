package org.jaybill.fast.im.common.dto;

import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
public class Keys {
    private PublicKey publicKey;
    private PrivateKey privateKey;
}
