package com.featureprobe.api.base.config;

import com.featureprobe.api.auth.tenant.TenantContext;
import com.featureprobe.api.entity.TenantSupport;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.featureprobe.api.repository",
        transactionManagerRef = "jpaTransactionManager")
public class JpaConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factory,
                                                                       JpaProperties properties) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.putAll(properties.getProperties());
        jpaProperties.put("hibernate.ejb.interceptor", hibernateInterceptor());
        return factory.dataSource(dataSource()).packages("com.featureprobe").properties(jpaProperties).build();
    }

    @Bean
    public EmptyInterceptor hibernateInterceptor() {
        return new EmptyInterceptor() {

            public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
                                  Type[] types) {
                if (entity instanceof TenantSupport) {
                    log.debug("[save] Updating the entity " + id + " with tenant information: " +
                            TenantContext.getCurrentTenant());
                    ((TenantSupport) entity).setOrganizeId(TenantContext.getCurrentTenant());
                }
                return false;
            }

            public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
                                 Type[] types) {
                if (entity instanceof TenantSupport) {
                    log.debug("[delete] Updating the entity " + id + " with tenant information: " +
                            TenantContext.getCurrentTenant());
                    ((TenantSupport) entity).setOrganizeId(TenantContext.getCurrentTenant());
                }
            }

            public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
                                        String[] propertyNames, Type[] types) {
                if (entity instanceof TenantSupport) {
                    log.debug("[flush-dirty] Updating the entity " + id + " with tenant information: " +
                            TenantContext.getCurrentTenant());
                    ((TenantSupport) entity).setOrganizeId(TenantContext.getCurrentTenant());
                }
                return false;
            }

        };
    }

    @Bean
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

}
