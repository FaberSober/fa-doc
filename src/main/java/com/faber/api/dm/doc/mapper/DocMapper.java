package com.faber.api.dm.doc.mapper;

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

    int syncDocViewChapterNumById(@Param("id") Integer id);

    int syncDocViewChapterNumByChapterId(@Param("chapterId") Integer chapterId);

}
