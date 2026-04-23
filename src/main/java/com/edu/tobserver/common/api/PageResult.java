package com.edu.tobserver.common.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageResult<T> {

    private final List<T> list;
    private final long total;
    private final int pageNum;
    private final int pageSize;
}
