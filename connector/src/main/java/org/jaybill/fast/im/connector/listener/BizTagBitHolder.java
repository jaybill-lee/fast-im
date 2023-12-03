package org.jaybill.fast.im.connector.listener;

import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BizTagBitHolder {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final Map<String, BitSet> map = new ConcurrentHashMap<>();

    public BitSet getBit(String bizId, String tag) {
        return map.computeIfAbsent(String.format("%s_%s", bizId, tag), (k) -> new BitSet(counter.getAndIncrement()));
    }
}
