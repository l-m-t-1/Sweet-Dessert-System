package com.sweet.dessertsystem.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationResourcesTests {

    @Test
    void migrationFilesArePackagedAndContainRequiredTables() throws IOException {
        String baseline = resource("db/migration/V1__baseline_core_schema.sql");
        String orders = resource("db/migration/V2__orders_and_stock_records.sql");
        String seed = resource("db/migration/V3__seed_demo_data.sql");

        assertThat(baseline)
                .contains("CREATE TABLE IF NOT EXISTS `user`")
                .contains("CREATE TABLE IF NOT EXISTS category")
                .contains("CREATE TABLE IF NOT EXISTS dessert");
        assertThat(orders)
                .contains("CREATE TABLE IF NOT EXISTS orders")
                .contains("CREATE TABLE IF NOT EXISTS order_detail")
                .contains("CREATE TABLE IF NOT EXISTS stock_record");
        assertThat(seed)
                .contains("WHERE NOT EXISTS")
                .contains("未分类");
    }

    private String resource(String path) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(stream).as("resource %s", path).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
