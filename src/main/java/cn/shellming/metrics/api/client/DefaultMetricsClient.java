package cn.shellming.metrics.api.client;

import cn.shellming.metrics.api.calculate.MetricCounter;
import cn.shellming.metrics.api.calculate.MetricTimer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认埋点实现，打印数据到控制台，仅作参考
 */
public class DefaultMetricsClient implements MetricsClient {
    private ScheduledExecutorService scheduledExecutorService;
    private int period;
    private boolean mute;

    public DefaultMetricsClient() {
        this(10000);
    }

    public DefaultMetricsClient(int period) {
        this(period, false);
    }

    public DefaultMetricsClient(int period, boolean mute) {
        System.out.println("default metrics client created");
        ThreadFactory springThreadFactory = new CustomizableThreadFactory("Metrics-pool-");
        scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, springThreadFactory);
        this.period = period;
        this.mute = mute;
    }

    @Override
    public MetricCounter counter(String metricsName, String description, SortedMap<String, String> tagMap) {
        return new MetricCounter() {
            private AtomicLong c = new AtomicLong(0);
            private String mn = metricsName;
            private String d = description;
            private SortedMap<String, String> t = tagMap;

            @Override
            public void increment() {
                if (!mute) {
                    System.out.println(// NOSONAR
                            "metric name: " + mn + ", description: " + d
                                    + (t != null && !t.isEmpty() ? ", tags: " + t.toString() : "") + ", counter: "
                                    + c.incrementAndGet());
                }
            }

            @Override
            public void incrementBy(long delta) {
                if (!mute) {
                    System.out.println(// NOSONAR
                            "metric name: " + mn + ", description: " + d
                                    + (t != null && !t.isEmpty() ? ", tags: " + t.toString() : "") + ", counter: "
                                    + c.addAndGet(delta));
                }
            }
        };
    }

    @Override
    public MetricTimer timer(String metricsName, String description, SortedMap<String, String> tagMap) {
        return new MetricTimer() {
            private int limit = 1_000_000;
            private SortedMap<Long, Long> storage = buildStorage();

            private SortedMap<Long, Long> buildStorage() {
                return Collections.synchronizedSortedMap(new TreeMap<>());
            }

            @Override
            public void record(long millis) {
                if (storage.size() >= limit) {
                    storage = buildStorage();
                }
                storage.put(System.currentTimeMillis(), millis);
                if (!mute) {
                    System.out.println("time consumed: " + millis + " ms."); // NOSONAR
                }
            }

            @Override
            public void record(long time, TimeUnit unit) {
                this.record(TimeUnit.MILLISECONDS.convert(time, unit));
            }
        };
    }

    @Override
    public void gauge(String metricsName, String description, SortedMap<String, String> tagMap,
                      Callable<Double> callable) {
        try {
            if (!mute) {
                scheduledExecutorService.scheduleWithFixedDelay(() -> {
                    try {
                        System.out.println(// NOSONAR
                                "metric name: " + metricsName + ", description: " + description
                                        + (tagMap != null && !tagMap.isEmpty() ? ", tags: " + tagMap.toString()
                                        : "")
                                        + ", value: " + callable.call());
                    } catch (Exception e) {
                        e.printStackTrace(); // NOSONAR
                    }
                }, period, 500L, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace(); // NOSONAR
        }
    }

}