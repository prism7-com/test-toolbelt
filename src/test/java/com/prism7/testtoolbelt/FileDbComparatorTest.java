package com.prism7.testtoolbelt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FileDbComparatorTest {


    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void test() {
        System.out.println("*** ファイル取り込み");
        FileDbImporter loader = new FileDbImporter(jdbcTemplate);
        loader.importFrom(Paths.get("src/test/resources/data/sample.txt").toString());

        System.out.println("*** 取り込み状態の確認");
        int result;
        FileDbComparator comparator = new FileDbComparator(jdbcTemplate);
        result = comparator.verifyExists(Paths.get("src/test/resources/data/verify_all_data.txt").toString());
        assertEquals(0, result);

        System.out.println("*** 一部レコードの削除");
        result = jdbcTemplate.update("DELETE FROM TestTable WHERE STRING_COLUMN IN ('String2', 'String4')");
        assertEquals(2, result);

        System.out.println("*** 削除状態の確認");
        result = comparator.verifyNotExists(Paths.get("src/test/resources/data/verify_deleted_data.txt").toString());
        assertEquals(1, result);
    }
}