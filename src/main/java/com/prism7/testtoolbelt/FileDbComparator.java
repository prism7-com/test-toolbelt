package com.prism7.testtoolbelt;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * ファイルとDBの内容を比較する
 */
public class FileDbComparator {

    private static final String SEPARATOR = ";";

    private final JdbcTemplate jdbcTemplate;

    public FileDbComparator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * ファイルとDBを比較し、存在することを検証する
     * @param filePath 比較を行うファイル
     * @return ファイルと異なった件数
     */
    public int verifyExists(String filePath) {
        int notFoundCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentTableName = null;
            List<String> fieldNames = null;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("[")) {
                    currentTableName = line.replaceAll("[\\[\\]]", "");
                } else if (line.startsWith("{")) {
                    fieldNames = parseFieldNames(line);
                } else {
                    if (fieldNames == null || fieldNames.isEmpty()) {
                        continue;
                    }
                    if (!verifyRecordExists(currentTableName, fieldNames, line)) {
                        System.out.printf("×: %s %s%n",currentTableName, line);
                        notFoundCount++;
                    } else {
                        System.out.printf("○: %s %s%n",currentTableName, line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notFoundCount;
    }

    /**
     * ファイルとDBを比較し、存在しないことを検証する
     * @param filePath 比較を行うファイル
     * @return ファイルと異なった件数
     */
    public int verifyNotExists(String filePath) {
        int foundCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentTableName = null;
            List<String> fieldNames = null;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("[")) {
                    currentTableName = line.replaceAll("[\\[\\]]", "");
                } else if (line.startsWith("{")) {
                    fieldNames = parseFieldNames(line);
                } else {
                    if (fieldNames == null || fieldNames.isEmpty()) {
                        continue;
                    }
                    if (verifyRecordExists(currentTableName, fieldNames, line)) {
                        System.out.printf("×: %s %s%n",currentTableName, line);
                        foundCount++;
                    } else {
                        System.out.printf("○: %s %s%n",currentTableName, line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foundCount;
    }

    private List<String> parseFieldNames(String line) {
        List<String> fieldNames = new ArrayList<>();
        String[] tokens = line.replaceAll("[{}]", "").split(SEPARATOR);
        for (String token : tokens) {
            fieldNames.add(token.trim());
        }
        return fieldNames;
    }

    private boolean verifyRecordExists(String tableName, List<String> fieldNames, String dataLine) {
        List<String> data = parseData(dataLine);
        List<String> whereConditions = new ArrayList<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            if (!fieldNames.get(i).endsWith("@")) {
                whereConditions.add(fieldNames.get(i) + " = '" + data.get(i) + "'");
            }
        }
        String sql = MessageFormat.format("SELECT * FROM {0} WHERE {1}", tableName, String.join(" AND ", whereConditions));
        List<?> result = jdbcTemplate.queryForList(sql);
        return !result.isEmpty();
    }

    private List<String> parseData(String line) {
        List<String> data = new ArrayList<>();
        String[] tokens = line.split(SEPARATOR);
        for (String token : tokens) {
            data.add(token.trim());
        }
        return data;
    }
}
