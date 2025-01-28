package com.kparlar.beam;

import com.amazonaws.services.sqs.model.Message;
import com.kparlar.beam.dto.FileDto;
import java.io.Serializable;
import java.util.*;
import org.apache.beam.sdk.values.TupleTag;

public class MessageConfigMapper implements Serializable {
    private final Map<Class<? extends FileDto>, MessageConfig> map = new HashMap<>();

    public static final MessageConfigMapper instance = new MessageConfigMapper();
    public MessageConfigMapper (){
    }

    public MessageConfigMapper put(Class<? extends FileDto> clazz, TupleTag<Message> retryTag, String inputQueue) {
        this.map.put(clazz, new MessageConfig(clazz, retryTag, inputQueue));
        return this;
    }

    public MessageConfig getConfig(Class<? extends FileDto> clazz) {
        return Objects.requireNonNull(map.get(clazz), String.format("No configuration found for this %s", clazz));
    }

    public List<Class<? extends FileDto>> getAllKeys(){
        return new ArrayList<>(map.keySet());
    }

}
