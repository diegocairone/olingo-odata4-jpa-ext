package com.cairone.olingo.ext.demo.cfg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class TokenStoreCfg {

	@Value("${tokenstore.datasource.driver-class-name}")
    private String driverClassName;
    
    @Value("${tokenstore.datasource.jdbc-url}")
    private String jdbcUrl;
    
    @Value("${tokenstore.datasource.username}")
    private String username;
    
    @Value("${tokenstore.datasource.password}")
    private String password;
    
    @Value("${tokenstore.datasource.hikari.connection-test-query}")
    private String connTestQuery;
    
    @Value("${tokenstore.datasource.hikari.minimum-idle}")
    private Integer minIdle;
    
    @Value("${tokenstore.datasource.hikari.maximum-pool-size}")
    private Integer maxPoolSize;
    
    @Bean
    public TokenStore getTokenStore() {
        
        HikariConfig config = new HikariConfig();
        
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTestQuery(connTestQuery);
        config.setMinimumIdle(minIdle);
        config.setMaximumPoolSize(maxPoolSize);
        
        HikariDataSource dataSource = new HikariDataSource(config);
        JdbcTokenStore tokenStore = new JdbcTokenStore(dataSource);
        
        return tokenStore;
    }
}
