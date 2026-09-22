package com.faber.api.dm.doc.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.faber.api.dm.doc.entity.Doc;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.api.dm.doc.mapper.DocChapterMapper;
import com.faber.core.exception.BuzzException;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.vo.tree.TreeNode;
import com.faber.core.web.biz.BaseTreeBiz;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.List;

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
        return getTree(scopePublicQuery(shareCode, query));
    }

    public DocChapter outGetById(String shareCode, Integer id) {
        Doc doc = docBiz.getPublicByShareCode(shareCode);
        DocChapter chapter = getById(id);
        if (chapter == null || !doc.getId().equals(chapter.getDocId())) {
            throw new BuzzException("章节未找到");
        }
        return chapter;
    }

    public TableRet<DocChapter> outPage(String shareCode, QueryParams query) {
        return selectPageByQuery(scopePublicQuery(shareCode, query));
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
