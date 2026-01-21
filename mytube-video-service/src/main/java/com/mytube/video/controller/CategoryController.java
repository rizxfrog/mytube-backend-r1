package com.mytube.video.controller;

import com.mytube.common.web.CustomResponse;
import com.mytube.video.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category/getall")
    public CustomResponse<Object> getAll() {
        return categoryService.getAll();
    }
}
