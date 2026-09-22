package com.faber.api.dm.doc.biz;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.api.dm.doc.entity.DocChapterDetail;
import com.faber.api.dm.doc.entity.DocChapterHisDetail;
import org.springframework.stereotype.Service;

import com.faber.api.dm.doc.entity.DocChapterHis;
import com.faber.api.dm.doc.mapper.DocChapterHisMapper;
import com.faber.core.exception.BuzzException;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.web.biz.BaseBiz;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * DOC-文档章节
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
@Service
public class DocChapterHisBiz extends BaseBiz<DocChapterHisMapper,DocChapterHis> {

    @Resource
    DocChapterBiz docChapterBiz;

    @Resource
    DocChapterHisDetailBiz docChapterHisDetailBiz;

    @Resource
    DocAccessBiz docAccessBiz;

    @Override
    public QueryWrapper<DocChapterHis> parseQuery(QueryParams query) {
        QueryWrapper<DocChapterHis> wrapper = super.parseQuery(query);
        Object docId = query.getQuery().get("docId");
        if (docId != null) {
            docAccessBiz.requireDocAccess(Convert.toInt(docId));
        }
        Object chapterId = query.getQuery().get("chapterId");
        if (chapterId != null) {
            docAccessBiz.requireChapterAccess(Convert.toInt(chapterId));
        }
        wrapper.in("doc_id", docAccessBiz.getAccessibleDocIds());
        return wrapper;
    }

    @Override
    public List<DocChapterHis> list() {
        return super.list(new QueryParams());
    }

    @Override
    protected void saveBefore(DocChapterHis entity) {
        if (entity.getId() == null) {
            docAccessBiz.requireDocAccess(entity.getDocId());
        } else {
            DocChapterHis existing = super.getById(entity.getId());
            if (existing == null) {
                throw new BuzzException("历史版本不存在");
            }
            docAccessBiz.requireDocAccess(existing.getDocId());
            if (entity.getDocId() != null && !entity.getDocId().equals(existing.getDocId())) {
                throw new BuzzException("历史版本所属文档不可修改");
            }
        }
        super.saveBefore(entity);
    }

    @Override
    public DocChapterHis getById(Serializable id) {
        DocChapterHis history = super.getById(id);
        if (history != null) {
            docAccessBiz.requireDocAccess(history.getDocId());
        }
        return history;
    }

    @Override
    public DocChapterHis getDetailById(Serializable id) {
        return getById(id);
    }

    @Override
    public <ID extends Serializable> List<DocChapterHis> getByIds(List<ID> ids) {
        List<DocChapterHis> histories = super.getByIds(ids);
        histories.forEach(history -> docAccessBiz.requireDocAccess(history.getDocId()));
        return histories;
    }

    @Override
    public boolean updateBatchById(Collection<DocChapterHis> entityList) {
        if (entityList == null) return true;
        for (DocChapterHis entity : entityList) {
            if (!updateById(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateBatchById(Collection<DocChapterHis> entityList, int batchSize) {
        return updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdate(DocChapterHis entity) {
        return entity.getId() == null ? save(entity) : updateById(entity);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapterHis> entityList) {
        if (entityList == null) return true;
        for (DocChapterHis entity : entityList) {
            if (!saveOrUpdate(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapterHis> entityList, int batchSize) {
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

    public DocChapterHis getTopByChapterId(int chapterId) {
        docAccessBiz.requireChapterAccess(chapterId);
        return getTop(
                lambdaQuery()
                        .eq(DocChapterHis::getChapterId, chapterId)
                        .orderByDesc(DocChapterHis::getId)
        );
    }

    /**
     * 判断保存历史记录
     * @param entity
     */
    @Transactional
    public void saveDocChapterDetailHis(DocChapterDetail entity) {
        // 判断是否与最新的历史版本一致，若一致，则不用重复保存
        DocChapterHis docChapterHis = this.getTopByChapterId(entity.getId());
        if (docChapterHis != null) {
            // 获取历史版本内容
            DocChapterHisDetail hisDetail = docChapterHisDetailBiz.getById(docChapterHis.getId());
            if (hisDetail != null && ObjUtil.equal(hisDetail.getContent(), entity.getContent())) {
                return;
            }
        }

        DocChapter docChapter = docChapterBiz.getById(entity.getId());

        // save his
        DocChapterHis his = new DocChapterHis();
        his.setDocId(docChapter.getDocId());
        his.setChapterId(docChapter.getId());
        this.save(his);

        // save his detail
        DocChapterHisDetail hisDetail = new DocChapterHisDetail();
        hisDetail.setId(his.getId());
        hisDetail.setContent(entity.getContent());
        docChapterHisDetailBiz.save(hisDetail);
    }

}
