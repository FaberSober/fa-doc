package com.faber.api.dm.doc.rest;

import com.faber.core.annotation.FaLogBiz;
import com.faber.core.web.rest.BaseController;
import com.faber.api.dm.doc.biz.DocChapterHisBiz;
import com.faber.api.dm.doc.entity.DocChapterHis;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DOC-文档章节
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:59
 */
@FaLogBiz("DOC-文档章节")
@RestController
@RequestMapping("/api/dm/doc/docChapterHis")
public class DocChapterHisController extends BaseController<DocChapterHisBiz, DocChapterHis, Integer> {

}