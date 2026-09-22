package com.faber.api.dm.doc.rest;

import cn.hutool.core.map.MapUtil;
import com.faber.api.dm.doc.biz.DocChapterDetailBiz;
import com.faber.api.dm.doc.entity.DocChapterDetail;
import com.faber.core.annotation.FaLogBiz;
import com.faber.core.annotation.FaLogOpr;
import com.faber.core.config.annotation.IgnoreUserToken;
import com.faber.core.enums.LogCrudEnum;
import com.faber.core.vo.msg.Ret;
import com.faber.core.web.rest.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DOC-文档章节详情
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:59
 */
@FaLogBiz("DOC-文档章节详情")
@RestController
@RequestMapping("/api/dm/doc/docChapterDetail")
public class DocChapterDetailController extends BaseController<DocChapterDetailBiz, DocChapterDetail, Integer> {

    @FaLogOpr(value = "查询或新增", crud = LogCrudEnum.R)
    @RequestMapping(value = "/getOrCreateById", method = RequestMethod.POST)
    @ResponseBody
    public Ret<DocChapterDetail> getOrCreateById(@RequestBody Map<String, Object> params) {
        Integer id = MapUtil.getInt(params, "id");
        DocChapterDetail o = baseBiz.getOrCreateById(id);
        return ok(o);
    }

    @IgnoreUserToken
    @FaLogOpr(value = "查询", crud = LogCrudEnum.R)
    @RequestMapping(value = "/outGetById/{shareCode}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Ret<DocChapterDetail> outGetById(@PathVariable("shareCode") String shareCode, @PathVariable("id") Integer id) {
        DocChapterDetail o = baseBiz.outGetById(shareCode, id);
        return ok(o);
    }

}
