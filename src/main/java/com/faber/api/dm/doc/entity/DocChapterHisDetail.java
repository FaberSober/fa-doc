package com.faber.api.dm.doc.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.faber.core.annotation.FaModalName;
import com.faber.core.bean.BaseCrtEntity;
import com.faber.core.bean.BaseDelEntity;
import lombok.Data;

import java.util.Date;


/**
 * DOC-章节历史详情
 * 
 * @author xu.pengfei
 * @email faberxu@gmail.com
 * @date 2023-07-14 11:00:32
 */
@FaModalName(name = "DOC-章节历史详情")
@TableName("dm_doc_chapter_his_detail")
@Data
public class DocChapterHisDetail extends BaseCrtEntity {
	
    @ColumnWidth(8)
    @ExcelProperty("ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ExcelProperty("章节历史富文本内容")
    private String content;

}
