package com.faber.api.dm.doc.rest;

import com.faber.api.dm.doc.biz.DocBiz;
import com.faber.api.dm.doc.entity.Doc;
import com.faber.core.annotation.FaLogBiz;
import com.faber.core.annotation.FaLogOpr;
import com.faber.core.annotation.LogNoRet;
import com.faber.core.config.annotation.IgnoreUserToken;
import com.faber.core.enums.LogCrudEnum;
import com.faber.core.vo.msg.Ret;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.QueryParams;
import com.faber.core.web.rest.BaseController;
import org.springframework.web.bind.annotation.*;

/**
 * DOC-文档
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:59
 */
@FaLogBiz("DOC-文档")
@RestController
@RequestMapping("/api/dm/doc/doc")
public class DocController extends BaseController<DocBiz, Doc, Integer> {

    @FaLogOpr(value = "分页查询", crud = LogCrudEnum.R)
    @LogNoRet
    @RequestMapping(value = "/pageMine", method = RequestMethod.POST)
    @ResponseBody
    public TableRet<Doc> pageMine(@RequestBody QueryParams query) {
        return baseBiz.pageMine(query);
    }

    @FaLogOpr(value = "id查询", crud = LogCrudEnum.R)
    @RequestMapping(value = "/getMineById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Ret<Doc> getMineById(@PathVariable Integer id) {
        Doc o = baseBiz.getMineById(id);
        return ok(o);
    }

    @IgnoreUserToken
    @FaLogOpr(value = "外网查询", crud = LogCrudEnum.R)
    @RequestMapping(value = "/outGetById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Ret<Doc> outGetById(@PathVariable Integer id) {
        Doc o = baseBiz.outGetById(id);
        return ok(o);
    }

    @IgnoreUserToken
    @FaLogOpr(value = "分享码查询", crud = LogCrudEnum.R)
    @RequestMapping(value = "/outGetByShareCode/{shareCode}", method = RequestMethod.GET)
    @ResponseBody
    public Ret<Doc> outGetByShareCode(@PathVariable String shareCode) {
        Doc o = baseBiz.outGetByShareCode(shareCode);
        return ok(o);
    }

}