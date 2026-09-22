package com.faber.api.dm.doc.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.faber.api.dm.doc.entity.DocChapterDetail;
import com.faber.api.dm.doc.mapper.DocChapterDetailMapper;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.web.biz.BaseBiz;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
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
    DocAccessBiz docAccessBiz;

    @Resource
    DocChapterHisBiz docChapterHisBiz;

    @Override
    public QueryWrapper<DocChapterDetail> parseQuery(QueryParams query) {
        QueryWrapper<DocChapterDetail> wrapper = super.parseQuery(query);
        wrapper.in("id", docAccessBiz.getAccessibleChapterIds());
        return wrapper;
    }

    @Override
    public List<DocChapterDetail> list() {
        return super.list(new QueryParams());
    }

    public DocChapterDetail getOrCreateById(Integer id) {
        docAccessBiz.requireChapterAccess(id);
        DocChapterDetail entity = super.getById(id);
        if (entity != null) return entity;

        // save if not exist
        entity = new DocChapterDetail();
        entity.setId(id);
        entity.setContent("");
        this.save(entity);
        return entity;
    }

    @Override
    public DocChapterDetail getById(Serializable id) {
        DocChapterDetail detail = super.getById(id);
        if (detail != null) {
            docAccessBiz.requireChapterAccess(id);
        }
        return detail;
    }

    @Override
    public DocChapterDetail getDetailById(Serializable id) {
        return getById(id);
    }

    @Override
    public <ID extends Serializable> List<DocChapterDetail> getByIds(List<ID> ids) {
        List<DocChapterDetail> details = super.getByIds(ids);
        details.forEach(detail -> docAccessBiz.requireChapterAccess(detail.getId()));
        return details;
    }

    @Override
    public boolean save(DocChapterDetail entity) {
        docAccessBiz.requireChapterAccess(entity.getId());
        return super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<DocChapterDetail> entityList) {
        if (entityList == null) return true;
        for (DocChapterDetail entity : entityList) {
            if (!save(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateById(DocChapterDetail entity) {
        docAccessBiz.requireChapterAccess(entity.getId());
        entity.setContent(cleanHtml(entity.getContent()));

        // 判断保存历史记录
        docChapterHisBiz.saveDocChapterDetailHis(entity);

        return super.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<DocChapterDetail> entityList) {
        if (entityList == null) return true;
        for (DocChapterDetail entity : entityList) {
            if (!updateById(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateBatchById(Collection<DocChapterDetail> entityList, int batchSize) {
        return updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdate(DocChapterDetail entity) {
        return entity.getId() == null ? save(entity) : updateById(entity);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapterDetail> entityList) {
        if (entityList == null) return true;
        for (DocChapterDetail entity : entityList) {
            if (!saveOrUpdate(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapterDetail> entityList, int batchSize) {
        return saveOrUpdateBatch(entityList);
    }

    @Override
    public boolean removeById(Serializable id) {
        docAccessBiz.requireChapterAccess(id);
        return super.removeById(id);
    }

    @Override
    public void removeBatchByIds(List<Serializable> ids) {
        if (ids == null) return;
        ids.forEach(this::removeById);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> ids) {
        if (ids == null || ids.isEmpty()) return true;
        ids.forEach(id -> removeById((Serializable) id));
        return true;
    }

    @Override
    public void removePerById(Serializable id) {
        docAccessBiz.requireChapterAccess(id);
        super.removePerById(id);
    }

    @Override
    public void removePerByIds(Collection<? extends Serializable> ids) {
        if (ids == null) return;
        ids.forEach(this::removePerById);
    }

    @Override
    public void removePerBatchByIds(List<Serializable> ids) {
        if (ids == null) return;
        ids.forEach(this::removePerById);
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
