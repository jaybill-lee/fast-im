package org.jaybill.fast.im.id.server;

import org.mariadb.jdbc.MariaDbPoolDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MariaPoolDataSourceGroup {
    private final List<MariaDbPoolDataSource> dataSources;

    public MariaPoolDataSourceGroup() {
        this.dataSources = new ArrayList<>();
    }

    public void addDataSource(MariaDbPoolDataSource source) {
        dataSources.add(source);
    }

    public void consume(Consumer<MariaDbPoolDataSource> consumer) {
        dataSources.forEach(consumer);
    }

    public MariaDbPoolDataSource get() {
        return dataSources.get(0);
    }
}
