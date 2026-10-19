package com.cmm.service.excel;

import java.util.ArrayList;
import java.util.List;

public class ExcelFileVO<T> {

    private final String fileName;
    private final List<T> rows = new ArrayList<>();
    private final List<RowError> errors = new ArrayList<>();

    public ExcelFileVO(String fileName) {
        this.fileName = fileName;
    }

    public void addRow(T row) {
        rows.add(row);
    }

    public void addError(int rowNumber, String message) {
        errors.add(new RowError(rowNumber, message));
    }

    public String getFileName() {
        return fileName;
    }

    public List<T> getRows() {
        return rows;
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public int getTotalCount() {
        return rows.size() + errors.size();
    }

    public int getSuccessCount() {
        return rows.size();
    }

    public int getErrorCount() {
        return errors.size();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public static class RowError {

        private final int rowNumber;
        private final String message;

        public RowError(int rowNumber, String message) {
            this.rowNumber = rowNumber;
            this.message = message;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public String getMessage() {
            return message;
        }
    }
}