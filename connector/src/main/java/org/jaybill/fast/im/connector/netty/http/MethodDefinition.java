package org.jaybill.fast.im.connector.netty.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodDefinition {
    private Object instance;
    private Method method;
}
