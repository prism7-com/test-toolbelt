package com.prism7.testtoolbelt;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvDbComparator {

    private static final String SEPARATOR = ";";

    private final JdbcTemplate jdbcTemplate;

    public CsvDbComparator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int verifyExists(String csvFilePath) {
        int notFoundCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String currentTableName = null;
            List<String> fieldNames = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[")) {
                    currentTableName = line.replaceAll("[\\[\\]]", "");
                } else if (line.startsWith("{")) {
                    fieldNames = parseFieldNames(line);
                } else {
                    if (fieldNames == null || fieldNames.isEmpty()) {
                        continue;
                    }
                    if (!verifyRecordExists(currentTableName, fieldNames, line)) {
                        System.out.println("×: " + line);
                        notFoundCount++;
                    } else {
                        System.out.println("○: " + line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notFoundCount;
    }

    public int verifyNotExists(String csvFilePath) {
        int foundCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String currentTableName = null;
            List<String> fieldNames = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[")) {
                    currentTableName = line.replaceAll("[\\[\\]]", "");
                } else if (line.startsWith("{")) {
                    fieldNames = parseFieldNames(line);
                } else {
                    if (fieldNames == null || fieldNames.isEmpty()) {
                        continue;
                    }
                    if (verifyRecordExists(currentTableName, fieldNames, line)) {
                        System.out.println("×: " + line);
                        foundCount++;
                    } else {
                        System.out.println("○: " + line);
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
        String sql = "SELECT * FROM " + tableName + " WHERE " + String.join(" AND ", whereConditions);
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
