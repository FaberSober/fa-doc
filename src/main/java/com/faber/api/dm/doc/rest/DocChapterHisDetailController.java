package com.faber.api.dm.doc.rest;

import com.faber.core.annotation.FaLogBiz;
import com.faber.core.web.rest.BaseController;
import com.faber.api.dm.doc.biz.DocChapterHisDetailBiz;
import com.faber.api.dm.doc.entity.DocChapterHisDetail;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DOC-章节历史详情
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-07-14 11:00:33
 */
@FaLogBiz("DOC-章节历史详情")
@RestController
@RequestMapping("/api/dm/doc/docChapterHisDetail")
public class DocChapterHisDetailController extends BaseController<DocChapterHisDetailBiz, DocChapterHisDetail, Integer> {

}