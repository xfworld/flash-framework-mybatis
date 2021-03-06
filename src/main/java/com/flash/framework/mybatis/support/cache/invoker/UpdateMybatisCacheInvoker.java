package com.flash.framework.mybatis.support.cache.invoker;

import com.flash.framework.mybatis.support.cache.CacheOperation;
import com.flash.framework.mybatis.support.cache.MybatisCache;
import com.flash.framework.mybatis.support.cache.MybatisCacheInvoker;
import com.flash.framework.mybatis.support.cache.MybatisCacheRegistry;
import com.flash.framework.mybatis.support.cache.generator.CacheKeyGenerator;
import com.flash.framework.mybatis.support.cache.handler.MybatisCacheUpdateHandler;
import com.google.common.base.Throwables;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author zhurg
 * @date 2020/4/27 - 8:56 PM
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(name = "mybatis.cache.enable", havingValue = "true")
public class UpdateMybatisCacheInvoker implements MybatisCacheInvoker {

    private final MybatisCacheRegistry mybatisCacheRegistry;

    private final ApplicationContext applicationContext;

    @Override
    public Object invoke(ProceedingJoinPoint joinPoint, MybatisCache mybatisCache, Method method, Object... args) throws Throwable {
        Object result = joinPoint.proceed();
        MybatisCacheUpdateHandler updateHandler = mybatisCacheRegistry.getCacheUpdateHandler(method.getName());
        if (Objects.nonNull(updateHandler)) {
            try {
                CacheKeyGenerator keyGenerator = applicationContext.getBean(mybatisCache.keyGenerator());
                String cacheKey = keyGenerator.generateKey(joinPoint.getTarget(), mybatisCache, method, args);
                updateHandler.updateCaches(cacheKey);
            } catch (Exception e) {
                log.error("[Flash Framework] mybatis update cache failed,cause:{}", Throwables.getStackTraceAsString(e));
            }
        }
        return result;
    }

    @Override
    public String operation() {
        return CacheOperation.UPDATE_BY_ID;
    }
}
