package ru.app.voicechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@EnableJpaRepositories(basePackages = "ru.app.voicechat.repositories")
public class JPAConfig {
}
