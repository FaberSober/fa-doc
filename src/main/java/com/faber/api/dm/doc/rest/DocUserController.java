package com.faber.api.dm.doc.rest;

import cn.hutool.core.map.MapUtil;
import com.faber.api.dm.doc.biz.DocUserBiz;
import com.faber.api.dm.doc.entity.DocUser;
import com.faber.api.dm.doc.vo.req.DocUserQueryVo;
import com.faber.api.dm.doc.vo.ret.DocUserRetVo;
import com.faber.core.annotation.FaLogBiz;
import com.faber.core.annotation.FaLogOpr;
import com.faber.core.annotation.LogNoRet;
import com.faber.core.enums.LogCrudEnum;
import com.faber.core.vo.msg.Ret;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.BasePageQuery;
import com.faber.core.web.rest.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DOC-文档用户
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:59
 */
@FaLogBiz("DOC-文档用户")
@RestController
@RequestMapping("/api/dm/doc/docUser")
public class DocUserController extends BaseController<DocUserBiz, DocUser, Integer> {

    @FaLogOpr("分页查询")
    @RequestMapping(value = "/pageVo", method = RequestMethod.POST)
    @ResponseBody
    @LogNoRet
    public TableRet<DocUserRetVo> pageVo(@RequestBody BasePageQuery<DocUserQueryVo> query) {
        return baseBiz.pageVo(query);
    }

    @FaLogOpr(value = "添加用户角色", crud = LogCrudEnum.C)
    @RequestMapping(value = "/addUsers", method = RequestMethod.POST)
    @ResponseBody
    @LogNoRet
    public Ret<Boolean> addUsers(@RequestBody Map<String, Object> params) {
        List<String> userIds = (List<String>) params.get("userIds");
        Integer docId = MapUtil.getInt(params, "docId");
        baseBiz.addUsers(userIds, docId);
        return ok();
    }

    @FaLogOpr(value = "批量添加用户", crud = LogCrudEnum.C)
    @RequestMapping(value = "/batchAddUsers", method = RequestMethod.POST)
    @ResponseBody
    @LogNoRet
    public Ret<Boolean> batchAddUsers(@RequestBody Map<String, Object> params) {
        List<String> userIds = (List<String>) params.get("userIds");
        List<Integer> docIds = (List<Integer>) params.get("docIds");
        baseBiz.batchAddUsers(userIds, docIds);
        return ok();
    }

    @FaLogOpr(value = "批量删除用户", crud = LogCrudEnum.C)
    @RequestMapping(value = "/batchRemoveUsers", method = RequestMethod.POST)
    @ResponseBody
    @LogNoRet
    public Ret<Boolean> batchRemoveUsers(@RequestBody Map<String, Object> params) {
        List<String> userIds = (List<String>) params.get("userIds");
        List<Integer> docIds = (List<Integer>) params.get("docIds");
        baseBiz.batchRemoveUsers(userIds, docIds);
        return ok();
    }

}