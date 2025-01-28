package com.kparlar.beam.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileDto implements Serializable {

    private String status;
    private String publishedDate;
    private String tryCount;
}
