package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ApiCsvImportResult extends ApiResult {

    @SerializedName("imported")
    private List<ImportResultSuccess> imported = new ArrayList<>();
    private List<ImportResultWarning> warnings = new ArrayList<>();
    private List<ImportResultError> errors = new ArrayList<>();

    public void addImported(final ApiIntent intent) {
        this.imported.add(new ImportResultSuccess(intent));
    }

    public void addWarning(final String warning) {
        this.warnings.add(new ImportResultWarning(warning));
    }

    public void addError(final String error) {
        this.errors.add(new ImportResultError(error));
    }

    public List<ImportResultSuccess> getImported() {
        return this.imported;
    }

    public List<ImportResultWarning> getWarnings() {
        return this.warnings;
    }

    public List<ImportResultError> getErrors() {
        return this.errors;
    }

    public static class ImportResultSuccess {
        @SerializedName("intent_name")
        private String intentName;
        @SerializedName("action")
        private String action;

        private transient ApiIntent intent;

        ImportResultSuccess(final ApiIntent intent) {
            this.intent = intent;
            this.intentName = intent.getIntentName();
        }

        public ApiIntent getIntent() {
            return this.intent;
        }

        public void setAction(final String action) {
            this.action = action;
        }

        public String getAction() {
            return this.action;
        }

        public String getIntentName() {
            return this.intentName;
        }
    }

    public static class ImportResultWarning {
        @SerializedName("warning")
        private String warning;

        ImportResultWarning(final String warning) {
            this.warning = warning;
        }
    }

    public static class ImportResultError {
        @SerializedName("error")
        private String error;

        ImportResultError(final String error) {
            this.error = error;
        }
    }
}
