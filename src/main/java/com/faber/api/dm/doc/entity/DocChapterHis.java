package com.faber.api.dm.doc.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.faber.core.annotation.FaModalName;
import com.faber.core.annotation.SqlEquals;
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
@TableName("dm_doc_chapter_his")
@Data
public class DocChapterHis extends BaseDelEntity {
	
    @ColumnWidth(8)
    @ExcelProperty("ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @SqlEquals
    @ExcelProperty("文档ID")
    private Integer docId;

    @SqlEquals
    @ExcelProperty("章节ID")
    private Integer chapterId;

    @ExcelProperty("版本名称")
    private String name;

}
