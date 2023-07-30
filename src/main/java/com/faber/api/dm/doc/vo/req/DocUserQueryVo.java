package com.faber.api.dm.doc.vo.req;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class DocUserQueryVo implements Serializable {

    private Integer docId;
    private String userId;
    private String name;
    private String username;

}
