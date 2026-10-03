package com.faber.api.dm.doc.mapper;

import com.faber.api.base.admin.entity.User;
import com.faber.api.dm.doc.entity.DocUser;
import com.faber.api.dm.doc.vo.req.DocUserQueryVo;
import com.faber.api.dm.doc.vo.ret.DocUserBatchVo;
import com.faber.api.dm.doc.vo.ret.DocUserRetVo;
import com.faber.core.config.mybatis.base.FaBaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * DOC-文档用户
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:57
 */
public interface DocUserMapper extends FaBaseMapper<DocUser> {

    List<DocUserRetVo> pageVo(@Param("query") DocUserQueryVo query, @Param("sorter") String sorter);

    List<User> getDocUserList(@Param("docId") Integer docId);

    /**
     * 批量查询多个文档的参与用户，结果带 docId 便于按文档分组，避免逐文档查询。
     */
    List<DocUserBatchVo> getDocUserListByDocIds(@Param("docIds") List<Integer> docIds);

    /**
     * 查询文档用户关联ID，不受逻辑删除限制。
     * dm_doc_user(doc_id, user_id) 唯一约束下，新增关联前需要清理含软删除的历史记录。
     */
    @Select("SELECT id FROM dm_doc_user WHERE doc_id = #{docId} AND user_id = #{userId}")
    List<Integer> selectIdsIgnoreLogic(@Param("docId") Integer docId, @Param("userId") String userId);

}
