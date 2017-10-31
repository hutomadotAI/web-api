package com.hutoma.api.containers.facebook;

import java.util.List;

public class FacebookNodeList {

    private List<FacebookNode> data;

    public FacebookNodeList(final List<FacebookNode> data) {
        this.data = data;
    }

    public List<FacebookNode> getData() {
        return this.data;
    }

}
