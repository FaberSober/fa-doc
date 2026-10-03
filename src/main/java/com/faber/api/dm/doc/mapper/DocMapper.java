package com.faber.api.dm.doc.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.faber.core.config.mybatis.base.FaBaseMapper;
import com.faber.api.dm.doc.entity.Doc;
import org.apache.ibatis.annotations.Param;

/**
 * DOC-文档
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
public interface DocMapper extends FaBaseMapper<Doc> {

    int addViewNum(@Param("id") Integer id);

    /**
     * 按分享码查询文档，忽略租户条件。
     * 公开分享页是匿名请求，没有租户上下文，必须先按分享码定位文档，再用文档自身租户作为后续查询上下文。
     */
    @InterceptorIgnore(tenantLine = "true")
    Doc selectPublicByShareCodeIgnoreTenant(@Param("shareCode") String shareCode);

    int syncDocViewChapterNumById(@Param("id") Integer id);

    int syncDocViewChapterNumByChapterId(@Param("chapterId") Integer chapterId);

}
