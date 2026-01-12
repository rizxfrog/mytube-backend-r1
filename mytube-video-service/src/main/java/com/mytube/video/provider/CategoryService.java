package com.mytube.video.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mytube.api.video.CategoryServiceApi;
import com.mytube.common.redis.RedisUtil;
import jdk.jfr.Category;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

@DubboService
public class CategoryService implements CategoryServiceApi {
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Category getCategoryById(String mcId, String scId) {
        // 从redis中获取最新数据
        Category category = redisUtil.getObject("category:" + mcId + ":" + scId, Category.class);
        // 如果redis中没有数据，就从mysql中获取并更新到redis
        if (category == null) {
            QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mc_id", mcId).eq("sc_id", scId);
            category = categoryMapper.selectOne(queryWrapper);
            if (category == null) {
                return new Category();    // 如果不存在则返回空
            }

            Category finalCategory = category;
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue("category:" + mcId + ":" + scId, finalCategory);  // 默认存活1小时
            }, taskExecutor);
        }
        return category;
    }
}
