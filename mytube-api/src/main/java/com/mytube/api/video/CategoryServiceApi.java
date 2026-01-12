package com.mytube.api.video;

import jdk.jfr.Category;

public interface CategoryServiceApi {
    Category getCategoryById(String mcId, String scId);
}
