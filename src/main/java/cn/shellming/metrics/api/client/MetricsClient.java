package cn.shellming.metrics.api.client;

import cn.shellming.metrics.api.calculate.MetricCounter;
import cn.shellming.metrics.api.calculate.MetricTimer;

import java.util.SortedMap;
import java.util.concurrent.Callable;

/**
 * 通用埋点接口，为了隔离不同的metrics埋点实现
 */
public interface MetricsClient {
	/**
	 * 注册并返回一个MetricCounter对象，累加器。
	 * 针对同种类型的埋点只需调用本方法一次，保存返回的counter重复使用即可。
	 *
	 * @param metricsName 埋点名
	 * @param description 埋点描述
	 * @param tagMap 定义埋点的相关标签，一般会添加到tsdb中用于区分记录
	 * @return
	 */
	MetricCounter counter(String metricsName, String description, SortedMap<String, String> tagMap);

	/**
	 * 注册并返回一个MetricTimer，计时器。用法建议可参考counter。
	 *
	 * @param metricsName 埋点名
	 * @param description 埋点描述
	 * @param tagMap 定义埋点的相关标签，一般会添加到tsdb中用于区分记录
	 * @return
	 */
	MetricTimer timer(String metricsName, String description, SortedMap<String, String> tagMap);

	/**
	 * 注册一个gauge类型的埋点，瞬时值
	 *
	 * @param metricsName 埋点名
	 * @param description 埋点描述
	 * @param tagMap 定义埋点的相关标签，一般会添加到tsdb中用于区分记录
	 * @param callable 封装如何计算值的逻辑闭包
	 */
	void gauge(String metricsName, String description, SortedMap<String, String> tagMap, Callable<Double> callable);

}