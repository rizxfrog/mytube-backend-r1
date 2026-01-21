package com.mytube.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.web.CustomResponse;
import com.mytube.video.domain.Category;
import com.mytube.video.dto.CategoryDTO;
import com.mytube.video.mapper.CategoryMapper;
import com.mytube.video.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("videoTaskExecutor")
    private Executor taskExecutor;

    @Override
    public CustomResponse getAll() {
        CustomResponse customResponse = new CustomResponse();
        List<CategoryDTO> sortedCategories = new ArrayList<>();
        try {
            sortedCategories = redisUtil.getAllList("categoryList", CategoryDTO.class);
            if (sortedCategories != null && !sortedCategories.isEmpty()) {
                customResponse.setData(sortedCategories);
                return customResponse;
            }
        } catch (Exception e) {
            log.warn("Failed to load category list from redis", e);
        }

        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        List<Category> list = categoryMapper.selectList(queryWrapper);
        Map<String, CategoryDTO> categoryDTOMap = new HashMap<>();
        for (Category category : list) {
            String mcId = category.getMcId();
            String scId = category.getScId();
            String mcName = category.getMcName();
            String scName = category.getScName();
            String descr = category.getDescr();
            List<String> rcmTag = new ArrayList<>();
            if (category.getRcmTag() != null) {
                rcmTag = Arrays.asList(category.getRcmTag().split("\n"));
            }
            if (!categoryDTOMap.containsKey(mcId)) {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setMcId(mcId);
                categoryDTO.setMcName(mcName);
                categoryDTO.setScList(new ArrayList<>());
                categoryDTOMap.put(mcId, categoryDTO);
            }
            Map<String, Object> scMap = new HashMap<>();
            scMap.put("mcId", mcId);
            scMap.put("scId", scId);
            scMap.put("scName", scName);
            scMap.put("descr", descr);
            scMap.put("rcmTag", rcmTag);
            categoryDTOMap.get(mcId).getScList().add(scMap);
        }

        List<String> sortOrder = Arrays.asList("anime", "guochuang", "douga", "game", "kichiku",
                "music", "dance", "cinephile", "ent", "knowledge",
                "tech", "information", "food", "life", "car",
                "fashion", "sports", "animal", "virtual");
        for (String mcId : sortOrder) {
            if (categoryDTOMap.containsKey(mcId)) {
                sortedCategories.add(categoryDTOMap.get(mcId));
            }
        }
        try {
            redisUtil.delValue("categoryList");
            redisUtil.setAllList("categoryList", sortedCategories);
        } catch (Exception e) {
            log.warn("Failed to store category list to redis", e);
        }
        customResponse.setData(sortedCategories);
        return customResponse;
    }

    @Override
    public Category getCategoryById(String mcId, String scId) {
        Category category = redisUtil.getObject("category:" + mcId + ":" + scId, Category.class);
        if (category == null) {
            QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mc_id", mcId).eq("sc_id", scId);
            category = categoryMapper.selectOne(queryWrapper);
            if (category == null) {
                return new Category();
            }
            Category finalCategory = category;
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("category:" + mcId + ":" + scId, finalCategory), taskExecutor);
        }
        return category;
    }
}
