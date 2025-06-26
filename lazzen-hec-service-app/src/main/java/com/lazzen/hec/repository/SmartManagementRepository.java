package com.lazzen.hec.repository;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.dto.SqYbAliasDto;
import com.lazzen.hec.exception.NoneExistException;
import com.lazzen.hec.form.SqYbAliasForm;
import com.lazzen.hec.mapper.CoreBreakerMapper;
import com.lazzen.hec.mapper.SqYbAliasMapper;
import com.lazzen.hec.po.CoreBreaker;
import com.lazzen.hec.po.SqYbAlias;
import com.sipa.boot.java8.common.archs.snowflake.IUidGenerator;
import com.sipa.boot.java8.common.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
@Component
@DS("smart-management")
@RequiredArgsConstructor
public class SmartManagementRepository {
    private final CoreBreakerMapper coreBreakerMapper;

    private final SqYbAliasMapper sqYbAliasMapper;

    private final MapperFacade mapperFacade;

    private final IUidGenerator uidGenerator;

    public String assertSnByDomainCode(String domainCode) throws NoneExistException {
        CoreBreaker coreBreaker = coreBreakerMapper.selectOne(
            new LambdaQueryWrapper<CoreBreaker>().eq(CoreBreaker::getDomainCode, domainCode).last("limit 1"));
        NoneExistException.assertExist(coreBreaker, domainCode);
        NoneExistException.assertExist(coreBreaker.getBreakerCode(), domainCode);
        return coreBreaker.getBreakerCode();
    }

    public List<SqYbAliasDto> sqAlias(int type) {
        return this.mapperFacade.mapAsList(
            sqYbAliasMapper.selectList(Wrappers.<SqYbAlias>lambdaQuery().eq(SqYbAlias::getType, type)),
            SqYbAliasDto.class);
    }

    public Boolean saveSqAlias(SqYbAliasForm form) {
        String name = form.getName();
        boolean exists = sqYbAliasMapper.exists(Wrappers.<SqYbAlias>lambdaQuery()
            .eq(SqYbAlias::getType, form.getType())
            .ne(SqYbAlias::getIdx, form.getIdx())
            .eq(SqYbAlias::getName, name));
        if (exists) {
            throw new BadRequestException("名称已存在");
        }

        String prefix = form.getType() == 1 ? BusinessConstants.Water.NAME_PREFIX : BusinessConstants.Steam.NAME_PREFIX;
        if (name.startsWith(prefix)) {
            String idx = name.substring(prefix.length());
            if (StringUtils.isNumeric(idx)) {
                Integer index = Integer.valueOf(idx);
                if (!index.equals(form.getIdx())) {
                    exists = sqYbAliasMapper.exists(Wrappers.<SqYbAlias>lambdaQuery()
                        .eq(SqYbAlias::getType, form.getType())
                        .eq(SqYbAlias::getIdx, index));
                    if (!exists) {
                        throw new BadRequestException("名称已存在");
                    }
                }
            }
        }

        SqYbAlias sqYbAlias = sqYbAliasMapper.selectOne(Wrappers.<SqYbAlias>lambdaQuery()
            .eq(SqYbAlias::getType, form.getType())
            .eq(SqYbAlias::getIdx, form.getIdx()));
        if (Objects.nonNull(sqYbAlias)) {
            sqYbAlias.setName(name);
            sqYbAliasMapper.updateById(sqYbAlias);
        } else {
            SqYbAlias map = this.mapperFacade.map(form, SqYbAlias.class);
            map.setId(uidGenerator.nextLid());
            sqYbAliasMapper.insert(map);
        }
        return true;
    }
}
