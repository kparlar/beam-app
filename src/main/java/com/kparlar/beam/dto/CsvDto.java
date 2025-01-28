package com.kparlar.beam.dto;

import java.io.Serializable;
import lombok.*;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CsvDto extends FileDto implements Serializable{

    private String id;
    private String fileName;


    public CsvDto(String id, String fileName, String status, String publishedDate, String tryCount) {
        super(status, publishedDate,  tryCount);
        this.id = id;
        this.fileName = fileName;
    }
}
