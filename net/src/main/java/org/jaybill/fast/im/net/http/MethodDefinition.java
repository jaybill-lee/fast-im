package org.jaybill.fast.im.net.http;

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
