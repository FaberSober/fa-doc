package com.faber.api.dm.doc.biz;

import cn.hutool.core.util.ObjUtil;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.api.dm.doc.entity.DocChapterDetail;
import com.faber.api.dm.doc.entity.DocChapterHisDetail;
import org.springframework.stereotype.Service;

import com.faber.api.dm.doc.entity.DocChapterHis;
import com.faber.api.dm.doc.mapper.DocChapterHisMapper;
import com.faber.core.web.biz.BaseBiz;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * DOC-文档章节
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
@Service
public class DocChapterHisBiz extends BaseBiz<DocChapterHisMapper,DocChapterHis> {

    @Resource
    DocChapterBiz docChapterBiz;

    @Resource
    DocChapterHisDetailBiz docChapterHisDetailBiz;

    public DocChapterHis getTopByChapterId(int chapterId) {
        return getTop(
                lambdaQuery()
                        .eq(DocChapterHis::getChapterId, chapterId)
                        .orderByDesc(DocChapterHis::getId)
        );
    }

    /**
     * 判断保存历史记录
     * @param entity
     */
    @Transactional
    public void saveDocChapterDetailHis(DocChapterDetail entity) {
        // 判断是否与最新的历史版本一致，若一致，则不用重复保存
        DocChapterHis docChapterHis = this.getTopByChapterId(entity.getId());
        if (docChapterHis != null) {
            // 获取历史版本内容
            DocChapterHisDetail hisDetail = docChapterHisDetailBiz.getById(docChapterHis.getId());
            if (hisDetail != null && ObjUtil.equal(hisDetail.getContent(), entity.getContent())) {
                return;
            }
        }

        DocChapter docChapter = docChapterBiz.getById(entity.getId());

        // save his
        DocChapterHis his = new DocChapterHis();
        his.setDocId(docChapter.getDocId());
        his.setChapterId(docChapter.getId());
        this.save(his);

        // save his detail
        DocChapterHisDetail hisDetail = new DocChapterHisDetail();
        hisDetail.setId(his.getId());
        hisDetail.setContent(entity.getContent());
        docChapterHisDetailBiz.save(hisDetail);
    }

}