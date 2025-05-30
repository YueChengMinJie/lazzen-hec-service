package com.lazzen.hec.repository;

import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lazzen.hec.exception.NoneExistException;
import com.lazzen.hec.mapper.CoreBreakerMapper;
import com.lazzen.hec.po.CoreBreaker;

import lombok.RequiredArgsConstructor;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
@Component
@DS("smart-management")
@RequiredArgsConstructor
public class SmartManagementRepository {
    private final CoreBreakerMapper coreBreakerMapper;

    public String assertSnByDomainCode(String domainCode) throws NoneExistException {
        CoreBreaker coreBreaker = coreBreakerMapper.selectOne(
            new LambdaQueryWrapper<CoreBreaker>().eq(CoreBreaker::getDomainCode, domainCode).last("limit 1"));
        NoneExistException.assertExist(coreBreaker, domainCode);
        NoneExistException.assertExist(coreBreaker.getBreakerCode(), domainCode);
        return coreBreaker.getBreakerCode();
    }
}
