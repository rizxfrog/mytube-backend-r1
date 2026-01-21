package com.mytube.video.service;

import com.mytube.common.web.CustomResponse;
import com.mytube.video.domain.Category;

public interface CategoryService {
    CustomResponse getAll();
    Category getCategoryById(String mcId, String scId);
}
