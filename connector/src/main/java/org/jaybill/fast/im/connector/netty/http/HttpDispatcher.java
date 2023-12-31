package org.jaybill.fast.im.connector.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jaybill.fast.im.common.util.JsonUtil;
import org.jaybill.fast.im.connector.util.AnnotationUtil;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

public class HttpDispatcher {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private final List<HttpFilter> filters = new CopyOnWriteArrayList<>();
    private final ThreadFactory threadFactory = Thread.ofVirtual().name("http-dispatcher-", 0).factory();

    public void addFilter(HttpFilter filter) {
        filters.add(filter);
    }

    /**
     * 1. Submit HTTP request to virtual threads; <br/>
     * 2. Find the corresponding Java method based on the "path" and "method" of HTTP; <br/>
     * 3. Execute the {@link HttpFilter#before(BaseHttpRequest)}; <br/>
     * 4. Complete the conversion of HTTP request parameters and request body to Java method input parameters; <br/>
     * 5. Reflection calling Java method; <br/>
     * 6. Execute the {@link HttpFilter#after(BaseHttpRequest, Object)} <br/>
     * 7. Return response <br/>
     *
     * @param req http request
     * @param res http response
     */
    public void service(BaseHttpRequest req, BaseHttpResponse res) {
        var bodyByteBuf = req.body();
        if (bodyByteBuf != null) {
            bodyByteBuf.retain();
        }
        threadFactory.newThread(() -> {
            try {
                this.doService(req, res);
            } finally {
                if (bodyByteBuf != null) {
                    bodyByteBuf.release();
                }
            }
        }).start();
    }

    private void doService(BaseHttpRequest req, BaseHttpResponse res) {
        // Mapping path, if we can not find endpoint, return 404
        var methodDefinition = HttpEndpointMappings.get(req.path(), req.method());
        if (methodDefinition == null) {
            res.response(HttpResponseStatus.NOT_FOUND, null, null);
            return;
        }

        for (var filter : filters) {
            var filterResult = filter.before(req);
            if (!filterResult.continued) {
                this.doFilterResponse(res, filterResult);
                return;
            }
        }

        var method = methodDefinition.getMethod();
        // Invoke business code, we use "DefaultParameterNameDiscoverer" to get real params name
        String[] methodParamRealNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        // Prepare params
        Parameter[] methodParams = method.getParameters();
        Object[] invokeParams = new Object[methodParams.length];
        for (int i = 0; i < methodParams.length; i++) {
            var methodParam = methodParams[i];
            var methodParamType = methodParam.getType();
            JsonBody jsonBodyAnno = AnnotationUtil.findAnnotationIncludeMeta(method, JsonBody.class);
            if (jsonBodyAnno == null) {
                try {
                    assert methodParamRealNames != null;
                    invokeParams[i] = this.prepareParams(req, methodParamType, methodParamRealNames[i]);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                         InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                var bodyByteBuf = req.body();
                var bodyStr = bodyByteBuf.toString(StandardCharsets.UTF_8);
                var bodyInstance = JsonUtil.fromJson(bodyStr, methodParamType);
                invokeParams[i] = bodyInstance;
            }
        }

        // Invoke method
        try {
            var returnObj = method.invoke(methodDefinition.getInstance(), invokeParams);

            // Can decorate the return value
            HttpFilter.Result filterResult = null;
            for (var filter : filters) {
                filterResult = filter.after(req, returnObj);
                if (!filterResult.continued) {
                    this.doFilterResponse(res, filterResult);
                    return;
                }
            }
            var status = filterResult != null && filterResult.status != null
                    ? filterResult.status
                    : HttpResponseStatus.OK;
            returnObj = filterResult != null && filterResult.jsonBody != null
                    ? filterResult.jsonBody
                    : returnObj;
            this.doResponse(res, status, returnObj);
        } catch (Exception e) {
            res.response(HttpResponseStatus.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    private Object prepareParams(BaseHttpRequest request, Class<?> methodParamType, String methodParamName)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var objWrapper = this.parseBaseType(request, methodParamType, methodParamName);
        if (!objWrapper.missType) {
            return objWrapper.value;
        }

        var fields = methodParamType.getDeclaredFields();
        var constructor = methodParamType.getConstructor();
        var instance = constructor.newInstance();
        for (var field : fields) {
            var fieldName = field.getName();
            var fieldType = field.getType();
            objWrapper = this.parseBaseType(request, fieldType, fieldName);
            if (objWrapper.value != null) {
                // todo: check
                field.setAccessible(true);
                field.set(instance, objWrapper.value);
            }
        }
        return instance;
    }

    private ObjWrapper parseBaseType(BaseHttpRequest request, Class<?> paramType, String paramName) {
        var value = request.param(paramName);
        if (value == null) {
            return new ObjWrapper(null);
        }
        // string
        if (String.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(value);
        }

        // base type
        if (Byte.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(Byte.parseByte(value));
        }
        if (Short.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(Short.parseShort(value));
        }
        if (Integer.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(Integer.parseInt(value));
        }
        if (Long.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(Long.parseLong(value));
        }
        if (Float.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(Float.parseFloat(value));
        }
        if (Double.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(Double.parseDouble(value));
        }
        if (Boolean.class.isAssignableFrom(paramType)) {
            return new ObjWrapper(Boolean.parseBoolean(value));
        }

        return new ObjWrapper(null, true);
    }

    private void doResponse(BaseHttpResponse res, HttpResponseStatus status, Object returnObj) {
        if (returnObj instanceof String respStr) {
            res.response(status, null,
                    Unpooled.wrappedBuffer(respStr.getBytes(StandardCharsets.UTF_8)));
        } else {
            var httpBody = JsonUtil.toJson(returnObj);
            res.response(status, null,
                    Unpooled.wrappedBuffer(httpBody.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void doFilterResponse(BaseHttpResponse res, HttpFilter.Result filterResult) {
        var status = filterResult.status != null
                ? filterResult.status
                : HttpResponseStatus.INTERNAL_SERVER_ERROR;
        var returnJson = filterResult.jsonBody != null ? JsonUtil.toJson(filterResult.jsonBody) : "{}";
        res.response(status, null, Unpooled.wrappedBuffer(returnJson.getBytes(StandardCharsets.UTF_8)));
    }

    private static class ObjWrapper {
        Object value;
        boolean missType;

        public ObjWrapper(Object value) {
            this.value = value;
        }

        public ObjWrapper(Object value, boolean missType) {
            this.value = value;
            this.missType = missType;
        }
    }
}
