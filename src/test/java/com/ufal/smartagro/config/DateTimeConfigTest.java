package com.ufal.smartagro.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeConfigTest {

    @Test
    @DisplayName("Given fuso padrão When init Then define TimeZone default para America/Maceio e provê beans")
    void givenDefaultZone_whenInit_thenConfiguresClockAndZoneId() throws Exception {
        DateTimeConfig config = new DateTimeConfig();
        Field timezoneField = DateTimeConfig.class.getDeclaredField("timezone");
        timezoneField.setAccessible(true);
        timezoneField.set(config, "America/Maceio");

        config.init();

        assertThat(TimeZone.getDefault().getID()).isEqualTo("America/Maceio");

        ZoneId zoneId = config.zoneId();
        assertThat(zoneId.getId()).isEqualTo("America/Maceio");

        Clock clock = config.clock(zoneId);
        assertThat(clock.getZone().getId()).isEqualTo("America/Maceio");
    }
}
