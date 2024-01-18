package com.faber.api.dm.doc.biz;

import com.faber.api.base.admin.entity.User;
import com.faber.api.dm.doc.entity.DocUser;
import com.faber.api.dm.doc.mapper.DocUserMapper;
import com.faber.api.dm.doc.vo.req.DocUserQueryVo;
import com.faber.api.dm.doc.vo.ret.DocUserRetVo;
import com.faber.core.vo.msg.TableRet;
import com.faber.core.vo.query.BasePageQuery;
import com.faber.core.web.biz.BaseBiz;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DOC-文档用户
 *
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:58
 */
@Service
public class DocUserBiz extends BaseBiz<DocUserMapper, DocUser> {

    public TableRet<DocUserRetVo> pageVo(BasePageQuery<DocUserQueryVo> query) {
        PageInfo<DocUserRetVo> info = PageHelper.startPage(query.getCurrent(), query.getPageSize())
                .doSelectPageInfo(() -> baseMapper.pageVo(query.getQuery(), query.getSorter()));
        return new TableRet<>(info);
    }

    public void batchAddUsers(List<String> userIds, List<Integer> docIds) {
        for (Integer docId : docIds) {
            addUsers(userIds, docId);
        }
    }

    public void addUsers(List<String> userIds, Integer docId) {
        for (String userId : userIds) {
            long count = lambdaQuery()
                    .eq(DocUser::getUserId, userId)
                    .eq(DocUser::getDocId, docId)
                    .count();
            if (count == 1) continue;

            if (count > 0) {
                lambdaUpdate()
                        .eq(DocUser::getUserId, userId)
                        .eq(DocUser::getDocId, docId)
                        .remove();
            }

            DocUser link = new DocUser();
            link.setUserId(userId);
            link.setDocId(docId);
            this.save(link);
        }
    }

    public void batchRemoveUsers(List<String> userIds, List<Integer> docIds) {
        for (Integer docId : docIds) {
            lambdaUpdate()
                    .eq(DocUser::getDocId, docId)
                    .in(DocUser::getUserId, userIds)
                    .remove();
        }
    }

    public List<User> getDocUserList(Integer docId) {
        return baseMapper.getDocUserList(docId);
    }

}