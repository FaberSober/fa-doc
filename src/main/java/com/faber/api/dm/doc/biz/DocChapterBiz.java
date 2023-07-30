package com.faber.api.dm.doc.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.api.dm.doc.mapper.DocChapterMapper;
import com.faber.core.web.biz.BaseTreeBiz;
import org.springframework.stereotype.Service;

/**
 * DOC-文档章节
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
@Service
public class DocChapterBiz extends BaseTreeBiz<DocChapterMapper, DocChapter> {

    @Override
    protected void enhanceTreeQueryForMaxSort(QueryWrapper<DocChapter> wrapper, DocChapter entity) {
        wrapper.eq("doc_id", entity.getDocId());
    }

    @Override
    public boolean save(DocChapter entity) {
        entity.setViewNum(0);
        return super.save(entity);
    }

}