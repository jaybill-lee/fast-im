package org.jaybill.fast.im.connector.netty.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod(HttpMethods.DELETE)
public @interface Delete {
    String path() default "";
}
