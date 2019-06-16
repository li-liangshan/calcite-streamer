package com.streamer.web.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.streamer.service.core.ServiceConstant;
import com.streamer.web.aop.SessionInterceptor;

import okhttp3.OkHttpClient;

@SpringBootApplication
@ComponentScan(basePackages = "com.streamer")
public class StreamerApplication extends WebMvcConfigurerAdapter {

	@Resource
	private Environment environment;

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/web/login");
		super.addViewControllers(registry);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		List<String> list = new ArrayList<String>();
		// 开放定制的请求
		list.add("/web/login");
		String patterns[] = new String[list.size()];
		registry.addInterceptor(new SessionInterceptor()).excludePathPatterns(list.toArray(patterns));
		super.addInterceptors(registry);
	}

	@Bean
	public ViewResolver viewResolver() {
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setCache(true);
		resolver.setSuffix(".html");
		resolver.setContentType("text/html; charset=UTF-8");
		resolver.setRequestContextAttribute("request");
		return resolver;
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() {
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setTemplateLoaderPath("classpath:/templates/");
		configurer.setDefaultEncoding("UTF-8");
		return configurer;
	}

	@Bean
	public OkHttpClient http() {
		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).build();
		return client;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl(environment.getProperty("spring.datasource.url"));
		dataSource.setUsername(environment.getProperty("spring.datasource.username"));
		dataSource.setPassword(environment.getProperty("spring.datasource.password"));
		dataSource.setInitialSize(ServiceConstant.INIT_SIZE);
		dataSource.setMaxTotal(ServiceConstant.CONN_SIZE);
		dataSource.setMaxOpenPreparedStatements(ServiceConstant.CONN_SIZE);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setValidationQueryTimeout(ServiceConstant.VALIDATE_TIME);
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource);
		return jdbcTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(StreamerApplication.class, args);
	}
}
