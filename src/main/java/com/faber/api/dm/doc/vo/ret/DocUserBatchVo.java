package com.faber.api.dm.doc.vo.ret;

import com.faber.api.base.admin.entity.User;
import lombok.Data;
import lombok.ToString;

/**
 * DOC-文档用户批量查询结果：用户信息 + 所属文档ID
 *
 * @author xu.pengfei
 */
@Data
@ToString
public class DocUserBatchVo extends User {

    /**
     * 所属文档ID
     */
    private Integer docId;

}
