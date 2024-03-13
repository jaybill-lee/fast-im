package org.jaybill.fast.im.net.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationUtil {

    /**
     * Find direct annotation in method and the meta annotation of the direct annotation. <br/>
     * Found the first one and return immediately.
     *
     * @param element
     * @param annoClazz
     * @return
     */
    public static <T extends Annotation> T findAnnotationIncludeMeta(AnnotatedElement element, Class<T> annoClazz) {
        var directAnnotation = element.getAnnotation(annoClazz);
        if (directAnnotation != null) {
            return directAnnotation;
        }
        return findMeta(element.getAnnotations(), annoClazz);
    }

    /**
     * Check if it exists multi annotation, we will search the direct annotation and meta annotation recursive.
     *
     * @param method
     * @param annoClazz
     * @return
     */
    public static boolean existMultiAnnotationIncludeMeta(Method method, Class<?> annoClazz) {
        List<Annotation> list = new ArrayList<>();
        return existMultiAnnotationIncludeMeta(method.getAnnotations(), annoClazz, list);
    }

    private static boolean existMultiAnnotationIncludeMeta(Annotation[] annotations, Class<?> annoClazz, List<Annotation> list) {
        for (Annotation anno : annotations) {
            if (anno.annotationType().getCanonicalName().startsWith("java")) {
                continue;
            }
            if (annoClazz.isAssignableFrom(anno.annotationType())) {
                list.add(anno);
                if (list.size() > 1) {
                    return true;
                }
            } else {
                boolean multi = existMultiAnnotationIncludeMeta(anno.annotationType().getAnnotations(), annoClazz, list);
                if (multi) {
                    return true;
                }
            }
        }
        return list.size() > 1;
    }

    private static <T extends Annotation> T findMeta(Annotation[] annotations, Class<T> annoClazz) {
        for (Annotation ann : annotations) {
            if (ann.annotationType().getCanonicalName().startsWith("java")) {
                continue;
            }
            if (annoClazz.isAssignableFrom(ann.annotationType())) {
                return (T) ann;
            }
            return findMeta(ann.annotationType().getAnnotations(), annoClazz);
        }

        return null;
    }
}
