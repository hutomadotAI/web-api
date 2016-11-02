package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.MeshVariable;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiMeshList extends ApiResult {

    List<MeshVariable> mesh;

    public ApiMeshList(List<MeshVariable> mesh) {
        this.mesh = mesh;
    }

    public List<MeshVariable> getMesh() {
        return this.mesh;
    }
}
