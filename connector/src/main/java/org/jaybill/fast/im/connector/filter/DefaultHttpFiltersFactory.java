package org.jaybill.fast.im.connector.filter;

import org.jaybill.fast.im.net.http.HttpFilter;
import org.jaybill.fast.im.net.spring.HttpFiltersFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultHttpFiltersFactory implements HttpFiltersFactory {

    @Autowired
    private AuthFilter authFilter;

    @Override
    public List<HttpFilter> getFilters() {
        return List.of(authFilter);
    }
}
