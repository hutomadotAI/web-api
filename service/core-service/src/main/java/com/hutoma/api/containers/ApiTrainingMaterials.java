package com.hutoma.api.containers;

/**
 * Created by pedrotei on 12/10/16.
 */
public class ApiTrainingMaterials extends ApiResult {
    private final String trainingFile;

    public ApiTrainingMaterials(final String trainingFile) {
        this.trainingFile = trainingFile;
    }

    public String getTrainingFile() {
        return this.trainingFile;
    }
}
