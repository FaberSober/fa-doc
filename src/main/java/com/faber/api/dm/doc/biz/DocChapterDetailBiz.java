package com.faber.api.dm.doc.biz;

import cn.hutool.http.HtmlUtil;
import com.faber.api.dm.doc.entity.DocChapterDetail;
import com.faber.api.dm.doc.mapper.DocChapterDetailMapper;
import com.faber.core.web.biz.BaseBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * DOC-文档章节详情
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
@Service
public class DocChapterDetailBiz extends BaseBiz<DocChapterDetailMapper,DocChapterDetail> {

    @Autowired
    private Executor executor;

    @Resource
    DocBiz docBiz;

    @Resource
    DocChapterBiz docChapterBiz;

    @Resource
    DocChapterHisBiz docChapterHisBiz;

    public DocChapterDetail getOrCreateById(Integer id) {
        DocChapterDetail entity = getById(id);
        if (entity != null) return entity;

        // save if not exist
        entity = new DocChapterDetail();
        entity.setId(id);
        entity.setContent("");
        this.save(entity);
        return entity;
    }

    @Override
    public boolean updateById(DocChapterDetail entity) {
        // 过滤HTML文本，防止XSS攻击
        entity.setContent(HtmlUtil.filter(entity.getContent()));

        // 判断保存历史记录
        docChapterHisBiz.saveDocChapterDetailHis(entity);

        return super.updateById(entity);
    }

    public DocChapterDetail outGetById(Integer id) {
        DocChapterDetail detail = super.getById(id);

        executor.execute(() -> {
            docChapterBiz.getBaseMapper().addViewNum(id);
            docBiz.getBaseMapper().syncDocViewChapterNumByChapterId(id);
        });

        return detail;
    }
}