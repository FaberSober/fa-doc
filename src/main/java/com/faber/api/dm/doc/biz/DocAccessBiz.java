package com.faber.api.dm.doc.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.faber.api.dm.doc.entity.Doc;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.api.dm.doc.entity.DocChapterHis;
import com.faber.api.dm.doc.entity.DocUser;
import com.faber.api.dm.doc.mapper.DocChapterHisMapper;
import com.faber.api.dm.doc.mapper.DocChapterMapper;
import com.faber.api.dm.doc.mapper.DocMapper;
import com.faber.api.dm.doc.mapper.DocUserMapper;
import com.faber.core.constant.CommonConstants;
import com.faber.core.context.BaseContextHandler;
import com.faber.core.exception.BuzzException;
import com.faber.core.exception.auth.UserNoPermissionException;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * DOC 资源权限校验。
 *
 * <p>菜单权限只控制接口入口，文档归属和参与关系在这里做二次校验。</p>
 */
@Service
public class DocAccessBiz {

    @Resource
    private DocMapper docMapper;

    @Resource
    private DocChapterMapper docChapterMapper;

    @Resource
    private DocChapterHisMapper docChapterHisMapper;

    @Resource
    private DocUserMapper docUserMapper;

    public Doc requireDocAccess(Integer docId) {
        if (docId == null) {
            throw new BuzzException("文档ID不能为空");
        }

        Doc doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new BuzzException("文档不存在");
        }
        if (isSuperAdmin() || StrUtil.equals(doc.getCrtUser(), getCurrentUserId())) {
            return doc;
        }

        long count = docUserMapper.selectCount(new LambdaQueryWrapper<DocUser>()
                .eq(DocUser::getDocId, docId)
                .eq(DocUser::getUserId, getCurrentUserId()));
        if (count > 0) {
            return doc;
        }

        throw new UserNoPermissionException("无权访问该文档");
    }

    public Doc requireDocAccess(Serializable docId) {
        return requireDocAccess(toInteger(docId, "文档ID"));
    }

    public DocChapter requireChapterAccess(Integer chapterId) {
        if (chapterId == null) {
            throw new BuzzException("章节ID不能为空");
        }

        DocChapter chapter = docChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BuzzException("章节不存在");
        }
        requireDocAccess(chapter.getDocId());
        return chapter;
    }

    public DocChapter requireChapterAccess(Serializable chapterId) {
        return requireChapterAccess(toInteger(chapterId, "章节ID"));
    }

    public DocChapterHis requireHistoryAccess(Integer historyId) {
        if (historyId == null) {
            throw new BuzzException("历史版本ID不能为空");
        }

        DocChapterHis history = docChapterHisMapper.selectById(historyId);
        if (history == null) {
            throw new BuzzException("历史版本不存在");
        }
        requireDocAccess(history.getDocId());
        return history;
    }

    public DocChapterHis requireHistoryAccess(Serializable historyId) {
        return requireHistoryAccess(toInteger(historyId, "历史版本ID"));
    }

    public DocUser requireDocUserAccess(Integer docUserId) {
        if (docUserId == null) {
            throw new BuzzException("文档用户关联ID不能为空");
        }

        DocUser docUser = docUserMapper.selectById(docUserId);
        if (docUser == null) {
            throw new BuzzException("文档用户关联不存在");
        }
        requireDocAccess(docUser.getDocId());
        return docUser;
    }

    public DocUser requireDocUserAccess(Serializable docUserId) {
        return requireDocUserAccess(toInteger(docUserId, "文档用户关联ID"));
    }

    public List<Integer> getAccessibleDocIds() {
        if (isSuperAdmin()) {
            return toIds(docMapper.selectList(new LambdaQueryWrapper<Doc>()
                    .select(Doc::getId)));
        }

        String userId = getCurrentUserId();
        if (StrUtil.isBlank(userId)) {
            return Collections.singletonList(0);
        }

        Set<Integer> ids = new LinkedHashSet<>();
        docMapper.selectList(new LambdaQueryWrapper<Doc>()
                        .select(Doc::getId)
                        .eq(Doc::getCrtUser, userId))
                .forEach(doc -> ids.add(doc.getId()));
        List<Integer> linkedDocIds = docUserMapper.selectList(new LambdaQueryWrapper<DocUser>()
                        .select(DocUser::getDocId)
                        .eq(DocUser::getUserId, userId))
                .stream()
                .map(DocUser::getDocId)
                .filter(Objects::nonNull)
                .toList();
        if (!linkedDocIds.isEmpty()) {
            docMapper.selectList(new LambdaQueryWrapper<Doc>()
                            .select(Doc::getId)
                            .in(Doc::getId, linkedDocIds))
                    .forEach(doc -> ids.add(doc.getId()));
        }

        return ids.isEmpty() ? Collections.singletonList(0) : new ArrayList<>(ids);
    }

    public List<Integer> getAccessibleChapterIds() {
        List<Integer> docIds = getAccessibleDocIds();
        return toIds(docChapterMapper.selectList(new LambdaQueryWrapper<DocChapter>()
                .select(DocChapter::getId)
                .in(DocChapter::getDocId, docIds)));
    }

    public List<Integer> getAccessibleHistoryIds() {
        List<Integer> docIds = getAccessibleDocIds();
        return toIds(docChapterHisMapper.selectList(new LambdaQueryWrapper<DocChapterHis>()
                .select(DocChapterHis::getId)
                .in(DocChapterHis::getDocId, docIds)));
    }

    private boolean isSuperAdmin() {
        return StrUtil.equals(CommonConstants.SUPER_ADMIN_ID, getCurrentUserId());
    }

    private String getCurrentUserId() {
        return BaseContextHandler.getUserId();
    }

    private Integer toInteger(Serializable value, String fieldName) {
        if (value == null) {
            throw new BuzzException(fieldName + "不能为空");
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new BuzzException(fieldName + "格式错误");
        }
    }

    private <T> List<Integer> toIds(List<T> entities) {
        List<Integer> ids = new ArrayList<>();
        for (T entity : entities) {
            if (entity instanceof Doc doc && doc.getId() != null) {
                ids.add(doc.getId());
            } else if (entity instanceof DocChapter chapter && chapter.getId() != null) {
                ids.add(chapter.getId());
            } else if (entity instanceof DocChapterHis history && history.getId() != null) {
                ids.add(history.getId());
            }
        }
        return ids.isEmpty() ? Collections.singletonList(0) : ids;
    }
}
