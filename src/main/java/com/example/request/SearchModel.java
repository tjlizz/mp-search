package com.example.request;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.lizeze.util.StringUtil;

import java.util.Iterator;
import java.util.List;


public class SearchModel<T> {

    private Integer pageIndex;
    private Integer pageSize;
    private List<Field> fields;
    private String orderField;
    private boolean isAsc;

    public IPage<T> getPage() {
        IPage<T> page = new Page<>(pageIndex, pageSize);
        if (!StringUtil.isEmptyOrNull(orderField)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setAsc(isAsc);
            orderItem.setColumn(orderField);
            page.orders().add(orderItem);
        }
        return page;

    }

    public QueryWrapper<T> getQueryModel() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        for (Iterator iter = this.fields.iterator(); iter.hasNext(); ) {
            Field field = (Field) iter.next();
            switch (field.getQueryMethod()) {
                case eq:
                    queryWrapper.eq(true, field.getName(), field.getValue());
                    break;
                case like:
                    queryWrapper.like(true, field.getName(), field.getValue());
            }
        }

        if (!StringUtil.isEmptyOrNull(orderField)) {
            queryWrapper.orderBy(true, isAsc, orderField);
        }
        return queryWrapper;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public boolean isAsc() {
        return isAsc;
    }

    public void setAsc(boolean asc) {
        isAsc = asc;
    }
}
