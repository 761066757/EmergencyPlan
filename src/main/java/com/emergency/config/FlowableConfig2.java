package com.emergency.config;

import org.flowable.engine.*;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author liao
 * @version 1.0
 * @description: Flowable流程引擎配置2 - 完全抛弃 starter，手动引入核心依赖
 * @date 2026
 */
//@Configuration
public class FlowableConfig2 {

    // 注入Spring Boot配置的数据源和事务管理器
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    // 构造器注入（Spring自动装配）
    public FlowableConfig2(DataSource dataSource, PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    /**
     * 配置流程引擎核心参数
     */
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();

        // 1. 配置核心依赖（必须）
        config.setDataSource(dataSource); // 复用Spring数据源
        config.setTransactionManager(transactionManager); // 绑定Spring事务管理器

        // 2. 自定义配置项（和你原来的需求一致）
        config.setAsyncExecutorActivate(false); // 关闭异步执行器（定时任务）
        config.setDatabaseSchemaUpdate("create"); // 关闭数据库自动更新 FIXME 首次启动设置create，创建表结构成功后设置为false
        // 可选：设置流程引擎名称（按需）
        config.setEngineName("emergency-process-engine");

        return config;
    }

    /**
     * 创建ProcessEngine实例（Flowable核心引擎）
     */
    @Bean
    public ProcessEngineFactoryBean processEngine() {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(springProcessEngineConfiguration());
        return factoryBean;
    }


    /**
     * 如果用 flowable-spring-boot-starter，Starter 会自动创建 ProcessEngine 并暴露这些服务 Bean；
     * 如果不用 Starter、手动配置 ProcessEngine，则需要手动将这些服务注册为 Spring Bean。
     */
    // ========== 手动注册Flowable核心服务 Bean ==========
    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }
}