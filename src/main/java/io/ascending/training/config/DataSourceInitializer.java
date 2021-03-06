package io.ascending.training.config;


import org.apache.commons.dbcp.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = "io.ascending.training.repository")
public class DataSourceInitializer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Environment environment;

    @Value("#{ applicationProperties['database.serverName'] }")
    protected String databaseUrl;

    @Value("#{ applicationProperties['database.username'] }")
    protected String databaseUserName = "";

    @Value("#{ applicationProperties['database.password'] }")
    protected String databasePassword = "";

    @Value("#{ applicationProperties['database.dataSourceClassName'] }")
    protected String driverClassName;

//    @Value("#{ environment['jdbc.validation.query'] }")
//    protected String databaseValidationQuery;

    @PostConstruct
    public void init(){
        String profile = environment.getActiveProfiles()[0];
        logger.info("database initialization with profile: "+profile);
    }



    @Bean(name = "dataSource")
    //TODO talk about connection pool
    public DataSource getDataSource(){
        DataSource dataSource = createDataSource();
        return dataSource;
    }

    @Bean
    public HibernateTransactionManager transactionManager(@Autowired SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

//    @Bean(name="transactionManager")
//    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory,@Autowired DataSource dataSource) {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setEntityManagerFactory(entityManagerFactory);
//        transactionManager.setDataSource(dataSource);
//        return transactionManager;
//    }

    private BasicDataSource createDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(databaseUrl);
        dataSource.setUsername(databaseUserName);
        dataSource.setPassword(databasePassword);
//        dataSource.setValidationQuery(databaseValidationQuery);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(1800000);
        dataSource.setNumTestsPerEvictionRun(3);
        dataSource.setMinEvictableIdleTimeMillis(1800000);
        return dataSource;
    }

    @Profile({"test","stage","prod"})
    @Bean(name="flyway",initMethod="migrate")
    public Flyway flywayDefault() {
        return setupFlyway();
    }

    @Profile({"dev","unit"})
    @Bean(name="flyway",initMethod = "validate")
    public Flyway flywayDev() {
        return setupFlyway();
    }


    @Bean(name="hibernate4AnnotatedSessionFactory")
    @DependsOn("flyway")
    @Profile({"dev","test","staging","prod"})
    public LocalSessionFactoryBean getLocalSessionFactoryBean(@Autowired DataSource dataSource){
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setPackagesToScan(new String[] { "io.ascending.training.domain","io.ascending.training.dao"});
        Properties props = getDefaultHibernate();
        sessionFactoryBean.setHibernateProperties(props);
        return sessionFactoryBean;
    }

    @Bean(name="hibernate4AnnotatedSessionFactory")
    @DependsOn("flyway")
    @Profile({"unit"})
    public LocalSessionFactoryBean getLocalSessionFactoryBeanUnit(@Autowired DataSource dataSource){
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setPackagesToScan(new String[] { "io.ascending.training.domain","io.ascending.training.dao"});
        Properties props = getDefaultHibernate();
        props.put("org.hibernate.flushMode","ALWAYS");
        sessionFactoryBean.setHibernateProperties(props);
        return sessionFactoryBean;
    }


//    @Bean(name="entityManagerFactory")
//    @DependsOn("flyway")
//    @Profile({"dev","test","stage","prod"})
//    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
//        LocalContainerEntityManagerFactoryBean factoryBean = setUpLocalContainerEntityManagerFactoryBean();
//        Properties props = new Properties();
//        props.put("hibernate.dialect", "org.hibernate.spatial.dialect.postgis.PostgisDialect");
//        props.put("hibernate.hbm2ddl.auto", "validate");
//        props.put("hibernate.physical_naming_strategy", "io.ascending.training.extend.hibernate.ImprovedNamingStrategy");
//        props.put("hibernate.connection.charSet","UTF-8");
//        props.put("hibernate.show_sql","false");
////            <property name="hibernate.ejb.interceptor" value="com.overture.family.repository.jpa.DBNullsFirstLastInteceptor"/>
//        factoryBean.setJpaProperties(props);
//
//        return factoryBean;
//    }
//
//    @Bean(name="entityManagerFactory")
//    @DependsOn("flyway")
//    @Profile("unit")
//    public LocalContainerEntityManagerFactoryBean entityUnitManagerFactoryBean() {
//        LocalContainerEntityManagerFactoryBean factoryBean = setUpLocalContainerEntityManagerFactoryBean();
//        Properties props = new Properties();
//        props.put("hibernate.dialect", "org.hibernate.spatial.dialect.postgis.PostgisDialect");
//        props.put("hibernate.hbm2ddl.auto", "validate");
//        props.put("hibernate.physical_naming_strategy", "io.ascending.training.extend.hibernate.ImprovedNamingStrategy");
//        props.put("hibernate.connection.charSet","UTF-8");
//        props.put("hibernate.show_sql","true");
//        props.put("org.hibernate.flushMode","ALWAYS");
////            <property name="hibernate.ejb.interceptor" value="com.overture.family.repository.jpa.DBNullsFirstLastInteceptor"/>
//        factoryBean.setJpaProperties(props);
//
//        return factoryBean;
//    }
//
//
//    private LocalContainerEntityManagerFactoryBean setUpLocalContainerEntityManagerFactoryBean(){
//        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
//        factoryBean.setDataSource(getDataSource());
//        factoryBean.setPackagesToScan(new String[] { "io.ascending.training.domain","io.ascending.training.repository" });
//        factoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
//        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        return factoryBean;
//    }

    private Flyway setupFlyway(){
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setLocations("classpath:db/migration/");
        flyway.setDataSource(getDataSource());
        return flyway;
    }

    public Properties getDefaultHibernate(){
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.spatial.dialect.postgis.PostgisDialect");
        props.put("hibernate.hbm2ddl.auto", "validate");
//        props.put("hibernate.physical_naming_strategy", "io.ascending.training.extend.hibernate.ImprovedNamingStrategy");
        props.put("hibernate.connection.charSet","UTF-8");
        props.put("hibernate.show_sql","true");
        return props;
    }
}
