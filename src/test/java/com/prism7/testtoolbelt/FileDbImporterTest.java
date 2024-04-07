package com.prism7.testtoolbelt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;

@SpringBootTest
@Transactional
class FileDbImporterTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void test() {
        FileDbImporter loader = new FileDbImporter(jdbcTemplate);
        loader.loadAndImport(Paths.get("src/test/resources/data/sample.txt").toString());
    }
}