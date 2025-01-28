package com.kparlar.beam.repository;

import com.kparlar.beam.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileRepository {

    int upsert(FileEntity fileEntity);
    FileEntity findById(String id);
}
