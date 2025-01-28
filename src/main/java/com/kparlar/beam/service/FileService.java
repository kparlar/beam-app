package com.kparlar.beam.service;

import com.kparlar.beam.entity.FileEntity;
import com.kparlar.beam.repository.FileRepository;
import com.kparlar.beam.util.DBConnectionUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;

@Slf4j
public class FileService {


    public int upsert(FileEntity fileEntity) throws SQLException{
       return DBConnectionUtil.runInTransactionWithNumericResult(sqlSession ->
             getRepository(sqlSession).upsert(fileEntity)
        );
    }

    private FileRepository getRepository(SqlSession sqlSession) {
        return sqlSession.getMapper(FileRepository.class);
    }

}
