package com.faber.api.dm.doc.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.faber.core.annotation.FaModalName;
import com.faber.core.bean.BaseDelEntity;
import lombok.Data;

import java.util.Date;


/**
 * DOC-文档
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:57
 */
@FaModalName(name = "DOC-文档")
@TableName("dm_doc")
@Data
public class Doc extends BaseDelEntity {
	
    @ColumnWidth(8)
    @ExcelProperty("ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ExcelProperty("文档名称")
    private String name;

    @ExcelProperty("是否公开")
    private Boolean isPublic;

    @ExcelProperty("分享码")
    private String shareCode;

    @ExcelProperty("访问次数")
    private Integer viewNum;

    @ExcelProperty("章节总访问次数")
    private Integer viewChapterNum;

}
