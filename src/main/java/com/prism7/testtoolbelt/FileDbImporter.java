package com.prism7.testtoolbelt;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.Logger.Level.ERROR;

public class FileDbImporter {

    private static final String SEPARATOR = ";";

    private final JdbcTemplate jdbcTemplate;

    public FileDbImporter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void loadAndImport(String csvFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {

            String line;
            String currentTableName = null;
            List<String> fieldNames = null;
            List<List<String>> dataLines = new ArrayList<>();

            // CSVファイルを読み込み、データを準備
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[")) {
                    if (currentTableName != null) {
                        performBulkInsert(currentTableName, fieldNames, dataLines);
                        dataLines.clear();
                    }
                    currentTableName = line.replaceAll("[\\[\\]]", "");
                } else if (line.startsWith("{")) {
                    fieldNames = parseFieldNames(line);
                } else {
                    dataLines.add(parseData(line));
                }
            }

            // 最後のテーブルのデータを挿入
            if (currentTableName != null && !dataLines.isEmpty()) {
                performBulkInsert(currentTableName, fieldNames, dataLines);
            }

            System.out.println("CSVファイルの読み込みとデータベースへの挿入が完了しました。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // フィールド名をパースするメソッド
    private List<String> parseFieldNames(String line) {
        return List.of(line.replaceAll("[{}]", "").split(SEPARATOR));
    }

    // データをパースするメソッド
    private List<String> parseData(String line) {
        return List.of(line.split(SEPARATOR));
    }

    // BulkInsertを実行するメソッド
    private void performBulkInsert(String tableName, List<String> fieldNames, List<List<String>> dataLines) {
        String insertSQL = generateInsertSQL(tableName, fieldNames);
        System.out.println("実行するSQL: " + insertSQL); // SQL文を標準出力に出力
        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                List<String> data = dataLines.get(i);
                for (int j = 0; j < data.size(); j++) {
                    preparedStatement.setString(j + 1, data.get(j));
                }
            }

            @Override
            public int getBatchSize() {
                return dataLines.size();
            }
        });
    }

    // INSERT文を生成するメソッド
    private String generateInsertSQL(String tableName, List<String> fieldNames) {
        String columns = String.join(", ", fieldNames);
        String placeholders = fieldNames.stream().map(f -> "?").reduce((a, b) -> a + ", " + b).orElse("");
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
    }
}
