package com.lazzen.hec.repository;


import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lazzen.hec.exception.NoneExistException;
import com.lazzen.hec.mapper.CoreBreakerMapper;
import com.lazzen.hec.po.CoreBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@DS("144px")
public class BreakerRepository {
    private final CoreBreakerMapper coreBreakerMapper;

    public String assertSnByDomainCode(String domainCode)throws NoneExistException {
        CoreBreaker coreBreaker = coreBreakerMapper.selectOne(new LambdaQueryWrapper<CoreBreaker>()
                .eq(CoreBreaker::getDomainCode, domainCode)
                .last("limit 1")
        );
        NoneExistException.assertExist(coreBreaker, domainCode);
        NoneExistException.assertExist(coreBreaker.getBreakerCode(), domainCode);
        return coreBreaker.getBreakerCode();
    }

}
