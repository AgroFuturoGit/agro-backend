package com.ufal.smartagro.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuração de data, hora e fuso horário da aplicação.
 * Garante que a JVM opere com fuso explícito (padrão: America/Maceio - UTC-3),
 * evitando discrepâncias de data perto da meia-noite em ambientes UTC.
 */
@Configuration
public class DateTimeConfig {

    public static final String DEFAULT_ZONE_ID = "America/Maceio";

    @Value("${app.timezone:" + DEFAULT_ZONE_ID + "}")
    private String timezone;

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timezone)));
    }

    @Bean
    public ZoneId zoneId() {
        return ZoneId.of(timezone);
    }

    @Bean
    public Clock clock(ZoneId zoneId) {
        return Clock.system(zoneId);
    }
}
