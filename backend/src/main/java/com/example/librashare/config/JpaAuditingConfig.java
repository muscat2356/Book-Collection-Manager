package com.example.librashare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @EnableJpaAuditing を有効にするためのconfigクラス
 * ┗Domainクラスでの日時更新自動セットを行うためのトリガークラス
 * @author furuyama
 * @since 2026-07-15
 * @see UserController
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

}
