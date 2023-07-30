package com.faber.api.dm.doc.mapper;

import com.faber.api.dm.doc.entity.DocUser;
import com.faber.api.dm.doc.vo.req.DocUserQueryVo;
import com.faber.api.dm.doc.vo.ret.DocUserRetVo;
import com.faber.core.config.mybatis.base.FaBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DOC-文档用户
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:57
 */
public interface DocUserMapper extends FaBaseMapper<DocUser> {

    List<DocUserRetVo> pageVo(@Param("query") DocUserQueryVo query, @Param("sorter") String sorter);

}
