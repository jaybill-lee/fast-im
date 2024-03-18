package org.jaybill.fast.im.net.spring;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.net.http.*;
import org.jaybill.fast.im.net.util.AnnotationUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Retrieve all classes annotated by {@link HttpEndpoint}, then parse the {@link HttpMethod} annotated method,
 * and place it in the {@link HttpEndpointMappings}
 */
@Slf4j
@Configuration
public class HttpEndpointBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var httpEndpointAnno = AnnotationUtils.findAnnotation(bean.getClass(), HttpEndpoint.class);
        if (httpEndpointAnno == null) {
            return bean;
        }

        var javaMethods = bean.getClass().getDeclaredMethods();
        for (var javaMethod : javaMethods) {
            if (AnnotationUtil.existMultiAnnotationIncludeMeta(javaMethod, HttpMethod.class)) {
                throw new IllegalArgumentException(
                        String.format("exist more than 1 @HttpMethod annotation in method: %s#%s",
                                bean.getClass().getCanonicalName(), javaMethod.getName()));
            }
            var httpPathBuilder = new StringBuilder(this.normalisePath(httpEndpointAnno.path()));
            var httpMethodAnno = AnnotationUtil.findAnnotationIncludeMeta(javaMethod, HttpMethod.class);
            if (httpMethodAnno != null) {
                var httpMethod = httpMethodAnno.value();
                String path = "";
                switch (httpMethod) {
                    case HttpMethods.GET ->
                            path = AnnotationUtil.findAnnotationIncludeMeta(javaMethod, Get.class).path();
                    case HttpMethods.POST ->
                            path = AnnotationUtil.findAnnotationIncludeMeta(javaMethod, Post.class).path();
                    case HttpMethods.PUT ->
                            path = AnnotationUtil.findAnnotationIncludeMeta(javaMethod, Put.class).path();
                    case HttpMethods.DELETE ->
                            path = AnnotationUtil.findAnnotationIncludeMeta(javaMethod, Delete.class).path();
                }
                httpPathBuilder.append(this.normalisePath(path));
                HttpEndpointMappings.set(httpPathBuilder.toString(), httpMethod, new MethodDefinition(bean, javaMethod));
                log.debug("http mapping, path:{}, method:{}#{}",
                        httpPathBuilder, bean.getClass().getCanonicalName(), javaMethod.getName());
            }
        }
        return bean;
    }

    private String normalisePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        var p = path;
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        if (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }
}
