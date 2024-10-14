package com.faber.api.dm.es01;

import cn.easyes.core.conditions.select.LambdaEsQueryWrapper;
import cn.easyes.core.core.EsWrappers;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HtmlUtil;
import com.faber.FaTestApp;
import com.faber.api.dm.doc.biz.DocBiz;
import com.faber.api.dm.doc.biz.DocChapterBiz;
import com.faber.api.dm.doc.biz.DocChapterDetailBiz;
import com.faber.api.dm.doc.entity.Doc;
import com.faber.api.dm.doc.entity.DocChapter;
import com.faber.api.dm.doc.entity.DocChapterDetail;
import com.faber.api.dm.es.entity.Document;
import com.faber.api.dm.es01.entity.Document01;
import com.faber.api.dm.es01.esmapper.Document01EsMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FaTestApp.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EasyEs01Test {

    @Resource
    DocBiz docBiz;

    @Resource
    DocChapterBiz docChapterBiz;

    @Resource
    DocChapterDetailBiz docChapterDetailBiz;

    @Resource
    Document01EsMapper document01EsMapper;

    @Test
    public void testInsert() {
        List<Doc> docList = docBiz.list();

        for (Doc doc : docList) {
            List<DocChapter> docChapterList = docChapterBiz.lambdaQuery().eq(DocChapter::getDocId, doc.getId()).list();

            List<Document01> documentList = new ArrayList<>();
            for (DocChapter docChapter : docChapterList) {
                DocChapterDetail docChapterDetail = docChapterDetailBiz.getById(docChapter.getId());

                Document01 document = new Document01();
                document.setId(docChapter.getId());
                document.setDocId(docChapter.getDocId());
                document.setTitle(docChapter.getName());
                document.setCrtName(docChapter.getCrtName());
                document.setCrtTime(DateUtil.formatLocalDateTime(docChapter.getCrtTime()));
                if (docChapterDetail != null) {
                    String content = HtmlUtil.cleanHtmlTag(docChapterDetail.getContent());
                    document.setContent(content);
                }
//                document01EsMapper.insert(document);
                documentList.add(document);
            }
            document01EsMapper.insertBatch(documentList);
        }
    }

    @Test
    public void testSearch() {
        // 测试查询 写法和MP一样 可以用链式,也可以非链式 根据使用习惯灵活选择即可
        List<Document01> document = EsWrappers.lambdaChainQuery(document01EsMapper)
                .like(Document01::getContent, "流程图")
                .list();
        System.out.println(document);
    }

    @Test
    public void testMatch() {
        // 测试查询 写法和MP一样 可以用链式,也可以非链式 根据使用习惯灵活选择即可
        List<Document01> document = EsWrappers.lambdaChainQuery(document01EsMapper)
                .match(Document01::getTitle, "测试代码")
                .list();
        System.out.println(document);
    }

    @Test
    public void testMatchQuery() {
        // 从多个指定字段中查询包含"流程示例"的数据
        LambdaEsQueryWrapper<Document01> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.multiMatchQuery("流程", Document01::getTitle, Document01::getContent);

        // 其中,默认的Operator为OR,默认的minShouldMatch为60% 这两个参数都可以按需调整,我们api是支持的 例如:
        // 其中AND意味着所有搜索的Token都必须被匹配,OR表示只要有一个Token匹配即可. minShouldMatch 80 表示只查询匹配度大于80%的数据
        // wrapper.multiMatchQuery("老王",Operator.AND,80,Document::getCustomField,Document::getContent);

        List<Document01> documents = document01EsMapper.selectList(wrapper);

//        List<Document01> document = EsWrappers.lambdaChainQuery(document01EsMapper)
//                .multiMatchQuery("流程示例", Document01::getTitle, Document01::getContent)
//                .list();
        System.out.println(documents);
    }

}
