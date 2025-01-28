package com.kparlar.beam.function.map;

import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.kparlar.beam.Configuration;
import com.kparlar.beam.dto.MessageDto;
import com.kparlar.beam.dto.FileDto;
import com.kparlar.beam.util.DBConnectionUtil;


import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MapMessageToCsvDtoDoFn extends DoFn<Message, MessageDto> {
    Logger logger = LoggerFactory.getLogger(MapMessageToCsvDtoDoFn.class);
    private final Class<? extends FileDto> clazz;
    private static Configuration configuration;
    public MapMessageToCsvDtoDoFn(Class<? extends FileDto> clazz){
        this.clazz = clazz;
    }
    @StartBundle
    public void startBundle(StartBundleContext c) {
        configuration = c.getPipelineOptions().as(Configuration.class);
        DBConnectionUtil.init(configuration);
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        try{
            Message message = c.element();
            var messageDto = MessageDto.builder().json(message.getBody())
                    .receiptHandler(message.getReceiptHandle())
                    .clazz(this.clazz).build();
            c.output(messageDto);
            AmazonSQSClientBuilder.defaultClient().deleteMessage(configuration.getQueueCsv(), message.getReceiptHandle());
        }catch (Exception e){
            logger.error("Error while converting message to MessageDto", e);
        }

    }




}
