package com.emergency.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

/**
 * @author liao
 * @version 1.0
 * @description: Flowable流程引擎配置
 * @date 2026
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        // 关闭Flowable的定时任务（按需开启）
        engineConfiguration.setAsyncExecutorActivate(false);
        // 配置数据源（复用SpringBoot的数据源）
        DataSource dataSource = engineConfiguration.getDataSource();
        engineConfiguration.setDataSource(dataSource);
        // 关闭Flowable的数据库自动更新（使用手动脚本）
        engineConfiguration.setDatabaseSchemaUpdate("false");
    }
}