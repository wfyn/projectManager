package com.sudy.vo;
import lombok.Data;

import java.util.List;

/**
        * 分页数据包装类
 * @param <T> 数据类型
 */
@Data
public class PageVO<T> {
    // 数据列表
    private List<T> list;

    // 总记录数
    private Long total;

}
