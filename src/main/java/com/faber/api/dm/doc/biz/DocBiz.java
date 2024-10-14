package com.faber.api.dm.doc.biz;

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

    @Override
    protected void saveBefore(Doc entity) {
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

    public Doc outGetByShareCode(String shareCode) {
        Doc doc = lambdaQuery()
                .eq(Doc::getShareCode, shareCode)
                .one();

        if (doc == null) throw new BuzzException("文档未找到");

        // 查询是否公开
        if (!doc.getIsPublic()) throw new BuzzException("文档未找到");

        // 文档访问次数累加
        baseMapper.addViewNum(doc.getId());

        return doc;
    }

    @Override
    public void decorateOne(Doc i) {
        List<User> userList = docUserBiz.getDocUserList(i.getId());
        i.setUserList(userList);
        i.setUserNameList(userList.stream().map(user -> user.getName()).collect(Collectors.toList()));
    }

    public TableRet<Doc> pageMine(QueryParams query) {
        // 查询账户有访问权限的文档
        List<Integer> docIds = docUserBiz.lambdaQuery()
                .eq(DocUser::getUserId, getCurrentUserId())
                .select(DocUser::getDocId)
                .list()
                .stream().map(i -> i.getDocId())
                .collect(Collectors.toList());
        if (docIds.isEmpty()) {
            return new TableRet<>();
        }

        query.getQuery().put("id#$in", docIds);
        return super.selectPageByQuery(query);
    }

    public Doc getMineById(Integer id) {
        Doc doc = super.getById(id);

        // 查询是否有访问权限
        long authCount = docUserBiz.lambdaQuery()
                .eq(DocUser::getDocId, doc.getId())
                .eq(DocUser::getUserId, getCurrentUserId())
                .count();

        if (authCount <= 0) {
            throw new BuzzException("无权访问");
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