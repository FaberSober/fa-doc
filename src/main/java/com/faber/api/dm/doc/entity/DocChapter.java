package com.faber.api.dm.doc.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.faber.core.annotation.*;
import com.faber.core.bean.BaseDelEntity;
import lombok.Data;

import java.util.Date;


/**
 * DOC-文档章节
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:57
 */
@FaModalName(name = "DOC-文档章节")
@TableName("dm_doc_chapter")
@Data
public class DocChapter extends BaseDelEntity {

    @SqlTreeId
    @ColumnWidth(8)
    @ExcelProperty("ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @SqlTreeParentId
    @SqlEquals
    @ExcelProperty("父ID")
    private Integer parentId;

    @SqlEquals
    @ExcelProperty("文档ID")
    private Integer docId;

    @SqlTreeName
    @ExcelProperty("章节名称")
    private String name;

    @SqlSorter
    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty("访问次数")
    private Integer viewNum;

}
