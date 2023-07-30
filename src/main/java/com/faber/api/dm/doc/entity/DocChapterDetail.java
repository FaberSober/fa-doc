package com.faber.api.dm.doc.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.faber.core.annotation.FaModalName;
import com.faber.core.bean.BaseUpdEntity;
import lombok.Data;


/**
 * DOC-文档章节详情
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-06-30 16:44:57
 */
@FaModalName(name = "DOC-文档章节详情")
@TableName("dm_doc_chapter_detail")
@Data
public class DocChapterDetail extends BaseUpdEntity {
	
    @ColumnWidth(8)
    @ExcelProperty("ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ExcelProperty("章节富文本内容")
    private String content;

}
