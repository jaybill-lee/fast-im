package org.jaybill.fast.im.net.spring;

import org.jaybill.fast.im.net.http.HttpFilter;

import java.util.List;

public interface HttpFiltersFactory {

    List<HttpFilter> getFilters();
}
