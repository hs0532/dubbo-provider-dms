package com.cnki.provider;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.cnki.provider.db.DynamicDataSource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("application.properties")
public class DruidConfiguration {

    private Logger logger = LoggerFactory.getLogger(DruidConfiguration.class);

    /**
     *  以下所有配置信息，均从application.porperties中读取。Value对应的变量值，即为配置文件中的key
     */
    
    // 数据库连接URL
    @Value("${spring.datasource.url}")
    private String dbUrl;

    // 数据库用户名
    @Value("${spring.datasource.username}")
    private String username;

    // 数据库密码
    @Value("${spring.datasource.password}")
    private String password;

    // 数据库驱动（java类包）
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    // 连接池初始化大小 
    @Value("${spring.datasource.initialSize}")
    private int initialSize;

    // 连接池最小值
    @Value("${spring.datasource.minIdle}")
    private int minIdle;

    // 连接池最大 值
    @Value("${spring.datasource.maxActive}")
    private int maxActive;

    // 配置获取连接等待超时的时间 
    @Value("${spring.datasource.maxWait}")
    private int maxWait;

    // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    // 配置一个连接在池中最小生存的时间，单位是毫秒
    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    // 用来验证数据库连接的查询语句,这个查询语句必须是至少返回一条数据的SELECT语句
    @Value("${spring.datasource.validationQuery}")
    private String validationQuery;

    // 检测连接是否有效
    @Value("${spring.datasource.testWhileIdle}")
    private boolean testWhileIdle;

    // 申请连接时执行validationQuery检测连接是否有效。做了这个配置会降低性能。
    @Value("${spring.datasource.testOnBorrow}")
    private boolean testOnBorrow;

    // 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    @Value("${spring.datasource.testOnReturn}")
    private boolean testOnReturn;

    // 是否缓存preparedStatement，也就是PSCache。
    @Value("${spring.datasource.poolPreparedStatements}")
    private boolean poolPreparedStatements;
    
    // 指定每个连接上PSCache的大小。
    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")
    private int maxPoolPreparedStatementPerConnectionSize;
    
    // 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙  
    @Value("${spring.datasource.filters}")
    private String filters;
    
    // 通过connectProperties属性来打开mergeSql功能；慢SQL记录    
    @Value("${spring.datasource.connectionProperties}")
    private String connectionProperties;

    
    ////////////////////////////////////////////////////////
    // Druid控制台配置：记录慢SQL 
    @Value("${spring.datasource.logSlowSql}")
    private String logSlowSql;

    /**
     * Durid控制台Servlet
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setServlet(new StatViewServlet());
        reg.addUrlMappings("/druid/*");
        reg.addInitParameter("loginUsername", username);
        reg.addInitParameter("loginPassword", password);
        reg.addInitParameter("logSlowSql", logSlowSql);
        return reg;
    }

    
    
    /**
     * Durid控制台过滤器，控制非法访问
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        filterRegistrationBean.addInitParameter("profileEnable", "true");
        return filterRegistrationBean;
    }
    @Bean
    public DynamicDataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = DynamicDataSource.getInstance();

        DruidDataSource defaultDataSource = new DruidDataSource();
        defaultDataSource.setUrl(dbUrl);
        defaultDataSource.setUsername(username);
        defaultDataSource.setPassword(password);
        defaultDataSource.setDriverClassName(driverClassName);
        defaultDataSource.setInitialSize(initialSize);
        defaultDataSource.setMinIdle(minIdle);
        defaultDataSource.setMaxActive(maxActive);
        defaultDataSource.setMaxWait(maxWait);
        defaultDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        defaultDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        defaultDataSource.setValidationQuery(validationQuery);
        defaultDataSource.setTestWhileIdle(testWhileIdle);
        defaultDataSource.setTestOnBorrow(testOnBorrow);
        defaultDataSource.setTestOnReturn(testOnReturn);
        defaultDataSource.setPoolPreparedStatements(poolPreparedStatements);
        defaultDataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        try {
        	defaultDataSource.setFilters(filters);
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter", e);
        }
        defaultDataSource.setConnectionProperties(connectionProperties);
        logger.info("get druid datasource");
        

        Map<Object,Object> map = new HashMap<>();
        map.put("default", defaultDataSource);
        dynamicDataSource.setTargetDataSources(map);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);

        return dynamicDataSource;
    }
    /*
    /**
     * Durid数据源属性设置
     * @return
     */
    /*
    @Bean
    public DataSource druidDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(dbUrl);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        try {
            datasource.setFilters(filters);
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter", e);
        }
        datasource.setConnectionProperties(connectionProperties);
        
        return datasource;
    }*/

}