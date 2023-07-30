package com.faber.api.dm.doc.mapper;

import com.faber.core.config.mybatis.base.FaBaseMapper;
import com.faber.api.dm.doc.entity.DocChapter;
import org.apache.ibatis.annotations.Param;

/**
 * DOC-文档章节
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
public interface DocChapterMapper extends FaBaseMapper<DocChapter> {

    int addViewNum(@Param("id") Integer id);

}
