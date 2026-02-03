package com.heritage.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.Instant;

@Data
public class ProjectExportDTO {
    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("项目名称")
    private String name;

    @ExcelProperty("简介")
    private String intro;

    @ExcelProperty("地区")
    private String region;

    @ExcelProperty("创建时间")
    private Instant createdAt;
}
