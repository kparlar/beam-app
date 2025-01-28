package com.kparlar.beam.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Date;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class FileEntity implements Serializable {

    private String id;
    private String fileName;
    private String status;
    private Date publishedDate;


}
