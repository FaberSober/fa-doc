package com.faber.api.dm.doc.rest;

import com.faber.api.dm.doc.biz.DocChapterBiz;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.core.annotation.FaLogBiz;
import com.faber.core.annotation.FaLogOpr;
import com.faber.core.annotation.LogNoRet;
import com.faber.core.config.annotation.IgnoreUserToken;
import com.faber.core.enums.LogCrudEnum;
import com.faber.core.vo.msg.Ret;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.vo.tree.TreeNode;
import com.faber.core.web.rest.BaseTreeController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DOC-文档章节
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:59
 */
@FaLogBiz("DOC-文档章节")
@RestController
@RequestMapping("/api/dm/doc/docChapter")
public class DocChapterController extends BaseTreeController<DocChapterBiz, DocChapter, Integer> {

    /**
     * 获取所有节点Tree
     * @return
     */
    @IgnoreUserToken
    @FaLogOpr(value = "查询树", crud = LogCrudEnum.R)
    @LogNoRet
    @RequestMapping(value = "/outGetTree/{shareCode}", method = RequestMethod.POST)
    @ResponseBody
    public Ret<List<TreeNode<DocChapter>>> outGetTree(@PathVariable("shareCode") String shareCode, @RequestBody QueryParams query) {
        List<TreeNode<DocChapter>> treeList = baseBiz.outGetTree(shareCode, query);
        return ok(treeList);
    }

    @IgnoreUserToken
    @FaLogOpr(value = "查询", crud = LogCrudEnum.R)
    @RequestMapping(value = "/outGetById/{shareCode}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Ret<DocChapter> outGetById(@PathVariable("shareCode") String shareCode, @PathVariable("id") Integer id) {
        DocChapter o = baseBiz.outGetById(shareCode, id);
        return ok(o);
    }

    @IgnoreUserToken
    @FaLogOpr(value = "分页查询", crud = LogCrudEnum.R)
    @LogNoRet
    @RequestMapping(value = "/outPage/{shareCode}", method = RequestMethod.POST)
    @ResponseBody
    public TableRet<DocChapter> outPage(@PathVariable("shareCode") String shareCode, @RequestBody QueryParams query) {
        return baseBiz.outPage(shareCode, query);
    }

}
