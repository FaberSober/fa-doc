package com.faber.api.dm.doc.biz;

import com.faber.api.dm.doc.entity.DocChapterDetail;
import com.faber.api.dm.doc.mapper.DocChapterDetailMapper;
import com.faber.core.web.biz.BaseBiz;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
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

    private static final String HTML_BASE_URI = "https://fa-doc.invalid/";
    private static final Safelist DOC_HTML_SAFELIST = Safelist.relaxed()
            .preserveRelativeLinks(true)
            .addTags("audio", "video", "source")
            .addAttributes("pre", "class")
            .addAttributes("code", "class")
            .addAttributes("audio", "src", "controls", "height", "width", "poster", "preload", "autoplay", "loop", "muted", "playsinline")
            .addAttributes("video", "src", "controls", "height", "width", "poster", "preload", "autoplay", "loop", "muted", "playsinline")
            .addAttributes("source", "src", "type")
            .addProtocols("audio", "src", "http", "https")
            .addProtocols("audio", "poster", "http", "https")
            .addProtocols("video", "src", "http", "https")
            .addProtocols("video", "poster", "http", "https")
            .addProtocols("source", "src", "http", "https");

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
        entity.setContent(cleanHtml(entity.getContent()));

        // 判断保存历史记录
        docChapterHisBiz.saveDocChapterDetailHis(entity);

        return super.updateById(entity);
    }

    private String cleanHtml(String content) {
        if (content == null) return null;
        return Jsoup.clean(content, HTML_BASE_URI, DOC_HTML_SAFELIST);
    }

    public DocChapterDetail outGetById(String shareCode, Integer id) {
        docChapterBiz.outGetById(shareCode, id);
        DocChapterDetail detail = super.getById(id);

        executor.execute(() -> {
            docChapterBiz.getBaseMapper().addViewNum(id);
            docBiz.getBaseMapper().syncDocViewChapterNumByChapterId(id);
        });

        return detail;
    }
}
