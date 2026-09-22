package com.faber.api.dm.doc.biz;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.faber.api.dm.doc.entity.Doc;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.api.dm.doc.mapper.DocChapterMapper;
import com.faber.core.exception.BuzzException;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.vo.tree.TreeNode;
import com.faber.core.vo.tree.TreePosChangeVo;
import com.faber.core.web.biz.BaseTreeBiz;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * DOC-文档章节
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
@Service
public class DocChapterBiz extends BaseTreeBiz<DocChapterMapper, DocChapter> {

    @Resource
    DocBiz docBiz;

    @Resource
    DocAccessBiz docAccessBiz;

    private final ThreadLocal<Boolean> publicQuery = ThreadLocal.withInitial(() -> false);

    @Override
    public QueryWrapper<DocChapter> parseQuery(QueryParams query) {
        QueryWrapper<DocChapter> wrapper = super.parseQuery(query);
        if (!isPublicQuery()) {
            Object docId = query.getQuery().get("docId");
            if (docId != null) {
                docAccessBiz.requireDocAccess(Convert.toInt(docId));
            }
            wrapper.in("doc_id", docAccessBiz.getAccessibleDocIds());
        }
        return wrapper;
    }

    @Override
    protected void enhanceTreeQuery(QueryWrapper<DocChapter> wrapper) {
        if (!isPublicQuery()) {
            wrapper.in("doc_id", docAccessBiz.getAccessibleDocIds());
        }
    }

    @Override
    public List<DocChapter> list() {
        return super.list(new QueryParams());
    }

    @Override
    protected void saveBefore(DocChapter entity) {
        if (entity.getId() == null) {
            docAccessBiz.requireDocAccess(entity.getDocId());
        } else {
            DocChapter existing = super.getById(entity.getId());
            if (existing == null) {
                throw new BuzzException("章节不存在");
            }
            docAccessBiz.requireDocAccess(existing.getDocId());
            if (entity.getDocId() != null && !Objects.equals(entity.getDocId(), existing.getDocId())) {
                throw new BuzzException("章节所属文档不可修改");
            }
        }
        super.saveBefore(entity);
    }

    @Override
    public DocChapter getById(Serializable id) {
        DocChapter chapter = super.getById(id);
        if (chapter != null) {
            docAccessBiz.requireDocAccess(chapter.getDocId());
        }
        return chapter;
    }

    @Override
    public DocChapter getDetailById(Serializable id) {
        return getById(id);
    }

    @Override
    public <ID extends Serializable> List<DocChapter> getByIds(List<ID> ids) {
        List<DocChapter> chapters = super.getByIds(ids);
        chapters.forEach(chapter -> docAccessBiz.requireDocAccess(chapter.getDocId()));
        return chapters;
    }

    @Override
    public boolean updateBatchById(Collection<DocChapter> entityList) {
        if (entityList == null) return true;
        for (DocChapter entity : entityList) {
            if (!updateById(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateBatchById(Collection<DocChapter> entityList, int batchSize) {
        return updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdate(DocChapter entity) {
        return entity.getId() == null ? save(entity) : updateById(entity);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapter> entityList) {
        if (entityList == null) return true;
        for (DocChapter entity : entityList) {
            if (!saveOrUpdate(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocChapter> entityList, int batchSize) {
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
        ids.forEach(docAccessBiz::requireChapterAccess);
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

    @Override
    public void changePos(List<TreePosChangeVo> list) {
        if (list == null || list.isEmpty()) return;
        for (TreePosChangeVo item : list) {
            DocChapter chapter = docAccessBiz.requireChapterAccess(Convert.toInt(item.getKey()));
            if (StrUtil.isNotBlank(item.getPid()) && !Objects.equals(getRootId().toString(), item.getPid())) {
                DocChapter parent = docAccessBiz.requireChapterAccess(Convert.toInt(item.getPid()));
                if (!Objects.equals(chapter.getDocId(), parent.getDocId())) {
                    throw new BuzzException("章节不能移动到其他文档");
                }
            }
        }
        super.changePos(list);
    }

    @Override
    public List<DocChapter> getAllChildrenFromNode(Serializable id) {
        docAccessBiz.requireChapterAccess(id);
        return super.getAllChildrenFromNode(id);
    }

    @Override
    protected void enhanceTreeQueryForMaxSort(QueryWrapper<DocChapter> wrapper, DocChapter entity) {
        wrapper.eq("doc_id", entity.getDocId());
    }

    @Override
    public boolean save(DocChapter entity) {
        entity.setViewNum(0);
        return super.save(entity);
    }

    public List<TreeNode<DocChapter>> outGetTree(String shareCode, QueryParams query) {
        return withPublicQuery(() -> getTree(scopePublicQuery(shareCode, query)));
    }

    public DocChapter outGetById(String shareCode, Integer id) {
        Doc doc = docBiz.getPublicByShareCode(shareCode);
        DocChapter chapter = super.getById(id);
        if (chapter == null || !doc.getId().equals(chapter.getDocId())) {
            throw new BuzzException("章节未找到");
        }
        return chapter;
    }

    public TableRet<DocChapter> outPage(String shareCode, QueryParams query) {
        return withPublicQuery(() -> selectPageByQuery(scopePublicQuery(shareCode, query)));
    }

    private <T> T withPublicQuery(Supplier<T> action) {
        publicQuery.set(true);
        try {
            return action.get();
        } finally {
            publicQuery.remove();
        }
    }

    private boolean isPublicQuery() {
        return Boolean.TRUE.equals(publicQuery.get());
    }

    private QueryParams scopePublicQuery(String shareCode, QueryParams query) {
        Doc doc = docBiz.getPublicByShareCode(shareCode);
        QueryParams scopedQuery = query == null ? new QueryParams() : query;
        if (scopedQuery.getQuery() == null) {
            scopedQuery.setQuery(new HashMap<>());
        }
        scopedQuery.getQuery().put("docId", doc.getId());
        return scopedQuery;
    }

}
