package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Entry;
import com.tan.warehouse2.bean.Role;

import java.util.List;
import java.util.Set;

public interface EntryService {


    int add(Entry e);

    Entry checkBatchByEntryNum(String entryNum);

    PageInfo<Entry> checkByCondition(Entry entry, Integer pageNum, Integer pageSize);

    PageInfo<Entry> getAllPage(Integer pageNum, Integer pageSize);

    int description(Entry entry);

    void deleteByLimit(String limit);

    PageInfo<Entry> checkByConditionForm(Entry entry, Integer pageNum, Integer pageSize, String limit);
}
