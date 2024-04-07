package com.prism7.testtoolbelt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;

@SpringBootTest
@Transactional
class CsvLoaderTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void test() {
        CsvLoader loader = new CsvLoader(jdbcTemplate);
        loader.loadData(Paths.get("src/test/resources/data/sample.txt").toString());
    }
}