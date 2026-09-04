package com.example.Book_Search.config;

import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.example.Book_Search.openresource.*;
import com.example.Book_Search.sourceconfig.*;

@Component
public class SourceBeanRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    
    private static final String DEFAULT_CONFIG_LOCATION = "classpath:sources-config.yaml";
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String configPath = environment.getProperty("app.sources.config-path", DEFAULT_CONFIG_LOCATION);
        List<SourceDefinition> definitions = SourceConfigLoader.load(configPath);

        for (SourceDefinition def : definitions) {
            BeanDefinitionBuilder builder;

            if (def instanceof ApiSourceDefinition apiDef) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(GenericApiResource.class)
                        .addConstructorArgValue(apiDef)
                        .addConstructorArgReference("restTemplate")
                        .addConstructorArgReference("objectMapper");
            } else if (def instanceof CrawlSourceDefinition crawlDef) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(GenericCrawlResource.class)
                        .addConstructorArgValue(crawlDef);
            } else {
                throw new IllegalStateException("Loai SourceDefinition khong duoc ho tro: " + def.getClass());
            }

            String beanName = "source_" + def.getRoutingKey().replace('.', '_');
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
}
