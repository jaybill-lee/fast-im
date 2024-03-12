package org.jaybill.fast.im.net.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

public interface HttpFilter {
    /**
     * Called before entering a method annotated by {@link HttpMethod}; <br/>
     * You can prevent the filter from continuing to execute by setting {@link BeforeResult#continued}=false,
     * if so, we will use {@link BeforeResult#status} and {@link BeforeResult#jsonBody} to directly return the response.
     */
    BeforeResult before(BaseHttpRequest req);

    /**
     * It's similar to {@link HttpFilter#before(BaseHttpRequest)}. <br/>
     * @param req       original http request
     * @param returnObj original return obj, you can decorate it
     * @return
     */
    Result after(BaseHttpRequest req, Object returnObj);

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    class BeforeResult extends Result {
        /**
         * If {@link BeforeResult#continued} is false, you can set the value;
         */
        String jsonBody;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    class Result {
        boolean continued;
        /**
         * If {@link BeforeResult#continued} is false, you need to set the value; <br/>
         * Default 500.
         */
        HttpResponseStatus status;
    }
}
