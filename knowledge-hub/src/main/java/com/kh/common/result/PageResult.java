package com.kh.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页返回体，字段命名与 docs/02 接口约定一致。
 */
@Data
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 总条数 */
    private long total;

    /** 当前页码 */
    private long page;

    /** 每页条数 */
    private long size;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setList(page.getRecords());
        r.setTotal(page.getTotal());
        r.setPage(page.getCurrent());
        r.setSize(page.getSize());
        return r;
    }

    public static <T> PageResult<T> of(List<T> list, long total, long page, long size) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setTotal(total);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
