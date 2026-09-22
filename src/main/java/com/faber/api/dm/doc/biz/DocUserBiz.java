package com.faber.api.dm.doc.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.faber.api.base.admin.entity.User;
import com.faber.api.dm.doc.entity.DocUser;
import com.faber.api.dm.doc.mapper.DocUserMapper;
import com.faber.api.dm.doc.vo.req.DocUserQueryVo;
import com.faber.api.dm.doc.vo.ret.DocUserRetVo;
import com.faber.core.exception.BuzzException;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.BasePageQuery;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.web.biz.BaseBiz;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * DOC-文档用户
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
@Service
public class DocUserBiz extends BaseBiz<DocUserMapper, DocUser> {

    @Resource
    DocAccessBiz docAccessBiz;

    @Override
    public QueryWrapper<DocUser> parseQuery(QueryParams query) {
        QueryWrapper<DocUser> wrapper = super.parseQuery(query);
        Object docId = query.getQuery().get("docId");
        if (docId != null) {
            docAccessBiz.requireDocAccess(Integer.valueOf(docId.toString()));
        }
        wrapper.in("doc_id", docAccessBiz.getAccessibleDocIds());
        return wrapper;
    }

    @Override
    public List<DocUser> list() {
        return super.list(new QueryParams());
    }

    @Override
    protected void saveBefore(DocUser entity) {
        if (entity.getId() == null) {
            docAccessBiz.requireDocAccess(entity.getDocId());
        } else {
            DocUser existing = super.getById(entity.getId());
            if (existing == null) {
                throw new BuzzException("文档用户关联不存在");
            }
            docAccessBiz.requireDocAccess(existing.getDocId());
            if (entity.getDocId() != null && !Objects.equals(entity.getDocId(), existing.getDocId())) {
                throw new BuzzException("文档用户关联所属文档不可修改");
            }
        }
        super.saveBefore(entity);
    }

    @Override
    public DocUser getById(Serializable id) {
        DocUser docUser = super.getById(id);
        if (docUser != null) {
            docAccessBiz.requireDocAccess(docUser.getDocId());
        }
        return docUser;
    }

    @Override
    public DocUser getDetailById(Serializable id) {
        return getById(id);
    }

    @Override
    public <ID extends Serializable> List<DocUser> getByIds(List<ID> ids) {
        List<DocUser> docUsers = super.getByIds(ids);
        docUsers.forEach(docUser -> docAccessBiz.requireDocAccess(docUser.getDocId()));
        return docUsers;
    }

    @Override
    public boolean updateBatchById(Collection<DocUser> entityList) {
        if (entityList == null) return true;
        for (DocUser entity : entityList) {
            if (!updateById(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean updateBatchById(Collection<DocUser> entityList, int batchSize) {
        return updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdate(DocUser entity) {
        return entity.getId() == null ? save(entity) : updateById(entity);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocUser> entityList) {
        if (entityList == null) return true;
        for (DocUser entity : entityList) {
            if (!saveOrUpdate(entity)) return false;
        }
        return true;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<DocUser> entityList, int batchSize) {
        return saveOrUpdateBatch(entityList);
    }

    @Override
    public boolean removeById(Serializable id) {
        docAccessBiz.requireDocUserAccess(id);
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
        docAccessBiz.requireDocUserAccess(id);
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

    public TableRet<DocUserRetVo> pageVo(BasePageQuery<DocUserQueryVo> query) {
        if (query == null || query.getQuery() == null || query.getQuery().getDocId() == null) {
            throw new BuzzException("文档ID不能为空");
        }
        docAccessBiz.requireDocAccess(query.getQuery().getDocId());
        PageInfo<DocUserRetVo> info = PageHelper.startPage(query.getCurrent(), query.getPageSize())
                .doSelectPageInfo(() -> baseMapper.pageVo(query.getQuery(), query.getSorter()));
        return new TableRet<>(info);
    }

    public void batchAddUsers(List<String> userIds, List<Integer> docIds) {
        for (Integer docId : docIds) {
            addUsers(userIds, docId);
        }
    }

    public void addUsers(List<String> userIds, Integer docId) {
        docAccessBiz.requireDocAccess(docId);
        for (String userId : userIds) {
            long count = lambdaQuery()
                    .eq(DocUser::getUserId, userId)
                    .eq(DocUser::getDocId, docId)
                    .count();
            if (count == 1) continue;

            if (count > 0) {
                lambdaUpdate()
                        .eq(DocUser::getUserId, userId)
                        .eq(DocUser::getDocId, docId)
                        .remove();
            }

            DocUser link = new DocUser();
            link.setUserId(userId);
            link.setDocId(docId);
            this.save(link);
        }
    }

    public void batchRemoveUsers(List<String> userIds, List<Integer> docIds) {
        for (Integer docId : docIds) {
            docAccessBiz.requireDocAccess(docId);
            lambdaUpdate()
                    .eq(DocUser::getDocId, docId)
                    .in(DocUser::getUserId, userIds)
                    .remove();
        }
    }

    public List<User> getDocUserList(Integer docId) {
        docAccessBiz.requireDocAccess(docId);
        return baseMapper.getDocUserList(docId);
    }

}
