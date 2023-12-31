package org.jaybill.fast.im.connector.netty.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface HttpFilter {
    /**
     * Called before entering a method annotated by {@link HttpMethod}; <br/>
     * You can prevent the filter from continuing to execute by setting {@link Result#continued}=false,
     * if so, we will use {@link Result#status} and {@link Result#jsonBody} to directly return the response.
     */
    Result before(BaseHttpRequest req);

    /**
     * It's similar to {@link HttpFilter#before(BaseHttpRequest)}. <br/>
     * @param req       original http request
     * @param returnObj original return obj, you can decorate it
     * @return
     */
    Result after(BaseHttpRequest req, Object returnObj);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Result {
        boolean continued;
        /**
         * If {@link Result#continued} is false, you need to set the value; <br/>
         * Default 500.
         */
        HttpResponseStatus status;
        /**
         * If {@link Result#continued} is false, you can set the value;
         */
        Object jsonBody;
    }
}
