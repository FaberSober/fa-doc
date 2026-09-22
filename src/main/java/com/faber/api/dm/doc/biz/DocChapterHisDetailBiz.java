package com.faber.api.dm.doc.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.faber.api.dm.doc.entity.DocChapterHisDetail;
import com.faber.api.dm.doc.mapper.DocChapterHisDetailMapper;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.web.biz.BaseBiz;

import jakarta.annotation.Resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * DOC-章节历史详情
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-07-14 11:00:32
 */
@Service
public class DocChapterHisDetailBiz extends BaseBiz<DocChapterHisDetailMapper,DocChapterHisDetail> {

    @Resource
    DocAccessBiz docAccessBiz;

    @Override
    public QueryWrapper<DocChapterHisDetail> parseQuery(QueryParams query) {
        QueryWrapper<DocChapterHisDetail> wrapper = super.parseQuery(query);
        wrapper.in("id", docAccessBiz.getAccessibleHistoryIds());
        return wrapper;
    }

    @Override
    public List<DocChapterHisDetail> list() {
        return super.list(new QueryParams());
    }

    @Override
    public DocChapterHisDetail getById(Serializable id) {
        DocChapterHisDetail detail = super.getById(id);
        if (detail != null) {
            docAccessBiz.requireHistoryAccess(id);
        }
        return detail;
    }

    @Override
    public DocChapterHisDetail getDetailById(Serializable id) {
        return getById(id);
    }

    @Override
    public <ID extends Serializable> List<DocChapterHisDetail> getByIds(List<ID> ids) {
        List<DocChapterHisDetail> details = super.getByIds(ids);
        details.forEach(detail -> docAccessBiz.requireHistoryAccess(detail.getId()));
        return details;
    }

    @Override
    public boolean save(DocChapterHisDetail entity) {
        docAccessBiz.requireHistoryAccess(entity.getId());
        return super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<DocChapterHisDetail> entityList) {
        if (entityList == null) return true;
        for (DocChapterHisDetail entity : entityList) {
            if (!save(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateById(DocChapterHisDetail entity) {
        docAccessBiz.requireHistoryAccess(entity.getId());
        return super.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<DocChapterHisDetail> entityList) {
        if (entityList == null) return true;
        for (DocChapterHisDetail entity : entityList) {
            if (!updateById(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateBatchById(Collection<DocChapterHisDetail> entityList, int batchSize) {
        return updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdate(DocChapterHisDetail entity) {
        return entity.getId() == null ? save(entity) : updateById(entity);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapterHisDetail> entityList) {
        if (entityList == null) return true;
        for (DocChapterHisDetail entity : entityList) {
            if (!saveOrUpdate(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapterHisDetail> entityList, int batchSize) {
        return saveOrUpdateBatch(entityList);
    }

    @Override
    public boolean removeById(Serializable id) {
        docAccessBiz.requireHistoryAccess(id);
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
        docAccessBiz.requireHistoryAccess(id);
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
}
