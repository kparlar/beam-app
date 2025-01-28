package com.kparlar.beam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class MessageDto implements Serializable {

    private String json;
    private String receiptHandler;
    private Class<? extends FileDto> clazz;

}
