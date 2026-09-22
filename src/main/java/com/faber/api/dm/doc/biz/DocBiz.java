package com.faber.api.dm.doc.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.faber.api.base.admin.entity.User;
import com.faber.api.dm.doc.entity.Doc;
import com.faber.api.dm.doc.entity.DocUser;
import com.faber.api.dm.doc.mapper.DocMapper;
import com.faber.core.exception.BuzzException;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.web.biz.BaseBiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DOC-文档
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:59
 */
@Service
public class DocBiz extends BaseBiz<DocMapper, Doc> {

    @Resource
    DocUserBiz docUserBiz;

    @Resource
    DocAccessBiz docAccessBiz;

    @Override
    public QueryWrapper<Doc> parseQuery(QueryParams query) {
        QueryWrapper<Doc> wrapper = super.parseQuery(query);
        wrapper.in("id", docAccessBiz.getAccessibleDocIds());
        return wrapper;
    }

    @Override
    public List<Doc> list() {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.in("id", docAccessBiz.getAccessibleDocIds());
        List<Doc> list = super.list(wrapper);
        decorateList(list);
        return list;
    }

    @Override
    protected void saveBefore(Doc entity) {
        if (entity.getId() != null) {
            docAccessBiz.requireDocAccess(entity.getId());
        }
        long count = lambdaQuery()
                .eq(Doc::getShareCode, entity.getShareCode())
                .ne(entity.getId() != null, Doc::getId, entity.getId())
                .count();
        if (count > 0) {
            throw new BuzzException("分享码重复，请重新选择");
        }
        super.saveBefore(entity);
    }

    @Override
    @Transactional
    public boolean save(Doc entity) {
        entity.setViewNum(0);
        super.save(entity);

        // add doc link
        DocUser docUser = new DocUser();
        docUser.setDocId(entity.getId());
        docUser.setUserId(getCurrentUserId());
        docUserBiz.save(docUser);

        return true;
    }

    @Override
    public Doc getById(Serializable id) {
        Doc doc = super.getById(id);
        if (doc != null) {
            docAccessBiz.requireDocAccess(doc.getId());
        }
        return doc;
    }

    @Override
    public Doc getDetailById(Serializable id) {
        Doc doc = getById(id);
        if (doc != null) {
            decorateOne(doc);
        }
        return doc;
    }

    @Override
    public <ID extends Serializable> List<Doc> getByIds(List<ID> ids) {
        List<Doc> docs = super.getByIds(ids);
        docs.forEach(doc -> docAccessBiz.requireDocAccess(doc.getId()));
        return docs;
    }

    @Override
    public boolean saveOrUpdate(Doc entity) {
        return entity.getId() == null ? save(entity) : updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<Doc> entityList) {
        if (entityList == null) return true;
        for (Doc entity : entityList) {
            if (!updateById(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateBatchById(Collection<Doc> entityList, int batchSize) {
        return updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Doc> entityList) {
        if (entityList == null) return true;
        for (Doc entity : entityList) {
            if (!saveOrUpdate(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Doc> entityList, int batchSize) {
        return saveOrUpdateBatch(entityList);
    }

    @Override
    public boolean removeById(Serializable id) {
        docAccessBiz.requireDocAccess(id);
        return super.removeById(id);
    }

    @Override
    public void removeBatchByIds(List<Serializable> ids) {
        if (ids == null) return;
        ids.forEach(docAccessBiz::requireDocAccess);
        super.removeBatchByIds(ids);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> ids) {
        if (ids == null || ids.isEmpty()) return true;
        ids.forEach(id -> docAccessBiz.requireDocAccess((Serializable) id));
        return super.removeBatchByIds(ids);
    }

    @Override
    public void removePerById(Serializable id) {
        docAccessBiz.requireDocAccess(id);
        super.removePerById(id);
    }

    @Override
    public void removePerByIds(Collection<? extends Serializable> ids) {
        if (ids == null) return;
        ids.forEach(docAccessBiz::requireDocAccess);
        super.removePerByIds(ids);
    }

    @Override
    public void removePerBatchByIds(List<Serializable> ids) {
        if (ids == null) return;
        ids.forEach(docAccessBiz::requireDocAccess);
        super.removePerBatchByIds(ids);
    }

    public Doc outGetByShareCode(String shareCode) {
        Doc doc = getPublicByShareCode(shareCode);

        // 文档访问次数累加
        baseMapper.addViewNum(doc.getId());

        return doc;
    }

    /**
     * 查询公开文档，但不增加文档访问次数。
     * 公开章节树、章节和详情接口复用此校验，避免只凭 docId/chapterId 访问内容。
     */
    public Doc getPublicByShareCode(String shareCode) {
        Doc doc = lambdaQuery()
                .eq(Doc::getShareCode, shareCode)
                .one();

        if (doc == null || !Boolean.TRUE.equals(doc.getIsPublic())) {
            throw new BuzzException("文档未找到");
        }

        return doc;
    }

    @Override
    public void decorateOne(Doc i) {
        List<User> userList = docUserBiz.getDocUserList(i.getId());
        i.setUserList(userList);
        i.setUserNameList(userList.stream().map(user -> user.getName()).collect(Collectors.toList()));
    }

    public TableRet<Doc> pageMine(QueryParams query) {
        query.getQuery().put("id#$in", docAccessBiz.getAccessibleDocIds());
        return super.selectPageByQuery(query);
    }

    public Doc getMineById(Integer id) {
        Doc doc = getById(id);
        if (doc == null) {
            throw new BuzzException("文档不存在");
        }
        return doc;
    }

    public Doc outGetById(Integer id) {
        Doc doc = super.getById(id);

        if (doc == null) throw new BuzzException("文档未找到");

        // 查询是否公开
        if (!doc.getIsPublic()) throw new BuzzException("文档未找到");

        // 文档访问次数累加
        baseMapper.addViewNum(doc.getId());

        return doc;
    }

    public void syncDocViewChapterNumById(Integer id) {
        baseMapper.syncDocViewChapterNumById(id);
    }

}
