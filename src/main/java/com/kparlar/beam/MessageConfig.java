package com.kparlar.beam;
import com.amazonaws.services.sqs.model.Message;
import com.kparlar.beam.dto.FileDto;
import lombok.Builder;
import org.apache.beam.sdk.values.TupleTag;

@Builder(toBuilder = true)
public record MessageConfig(Class<? extends FileDto> clazz, TupleTag<Message> retryTag, String inputQueue) {
}
