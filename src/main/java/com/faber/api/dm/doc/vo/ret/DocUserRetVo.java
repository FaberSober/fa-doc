package com.faber.api.dm.doc.vo.ret;

import com.faber.api.dm.doc.entity.DocUser;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DocUserRetVo extends DocUser {

    private String name;
    private String username;

}
