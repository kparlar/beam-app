package com.kparlar.beam;

import com.amazonaws.services.sqs.model.Message;
import com.kparlar.beam.dto.CsvDto;
import org.apache.beam.sdk.values.TupleTag;

public class PipelineTags {

    public static final TupleTag<CsvDto> STATUS_PENDING = new TupleTag<CsvDto>(){};
    public static final TupleTag<CsvDto>  STATUS_COMPLETED = new TupleTag<CsvDto>(){};
    public static final TupleTag<Message> CSV_FOR_RETRY = new TupleTag<Message>(){};



}
