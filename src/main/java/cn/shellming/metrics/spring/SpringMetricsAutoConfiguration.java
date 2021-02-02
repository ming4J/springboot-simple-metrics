package cn.shellming.metrics.spring;


import cn.shellming.metrics.api.client.MetricsClient;
import cn.shellming.metrics.spring.client.SpringMetricsClient;
import cn.shellming.metrics.spring.config.SpringMetricsProperties;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author shaoming
 */
@Configuration
@ConditionalOnClass(MetricsClient.class)
@EnableConfigurationProperties(SpringMetricsProperties.class)
@Profile("!prod")
public class SpringMetricsAutoConfiguration {
	@Bean("springMetricsClient")
	@ConditionalOnMissingBean
	public MetricsClient springMetricsClient() {
		return new SpringMetricsClient();
	}

	@Bean
	public SpringMetricsRegistry springMetricsRegister() {
		return new SpringMetricsRegistry();
	}

	@Bean
	public CountedAspect countedAspect(MeterRegistry registry) {
		return new CountedAspect(registry);
	}

	@Bean
	MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName){
		return registry -> registry
				.config()
				.commonTags("application", applicationName);
	}

}
