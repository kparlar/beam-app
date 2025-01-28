package com.kparlar.beam;


import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.kparlar.beam.dto.CsvDto;
import com.kparlar.beam.dto.MessageDto;
import com.kparlar.beam.function.map.MapMessageToCsvDtoDoFn;
import com.kparlar.beam.transform.PersistMessageTransform;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.aws.sqs.SqsIO;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

@Slf4j
public class PipelineFactory {
    /*private static Logger logger = LoggerFactory.getLogger(PipelineFactory.class);*/
    private static AmazonSQS amazonSqs = AmazonSQSClientBuilder.defaultClient();
    static Pipeline createPipeline(Configuration configuration) {

        log.info("Create Pipeline Entered");
        final Pipeline pipeline = Pipeline.create(configuration);
        MessageConfigMapper messageConfigMapper = configureMessageMapping(configuration);

        //Receive Message
        final PCollection<MessageDto>  messagesReceived = receiveMessage(pipeline, messageConfigMapper);

        //Persist
        messagesReceived.apply("Persist message", new PersistMessageTransform());

        return pipeline;
    }

    private static PCollection<MessageDto> receiveMessage(Pipeline pipeline, MessageConfigMapper messageConfigMapper) {
        List<PCollection<MessageDto>> pCollectionList =  messageConfigMapper.getAllKeys().stream().map(messageConfigMapper::getConfig).map(messageConfig -> createIncomingMessageStream(pipeline, messageConfig)).toList();
        PCollectionList<MessageDto> allPCollections = null;
        for (PCollection<MessageDto> pCollection : pCollectionList) {
            if (allPCollections == null) {
                allPCollections = PCollectionList.of(pCollection);
            } else {
                allPCollections = allPCollections.and(pCollection);
            }
        }

        assert allPCollections != null;
        return allPCollections.apply("Combine all incoming Dtos", Flatten.pCollections());


    }

    private static PCollection<MessageDto> createIncomingMessageStream(Pipeline pipeline, MessageConfig messageConfig) {
        final String messageType = messageConfig.getClass().getSimpleName();

        //PCollection<SqsMessage> messageFromInputTopic = pipeline.apply(String.format("Read %s messages from input topic", messageType), SqsIO.read().withQueueUrl(messageConfig.inputQueue()));
        PCollection<Message> messageFromInputTopic = pipeline.apply(String.format("Read %s messages from input topic", messageType), SqsIO.read().withQueueUrl(messageConfig.inputQueue()));

        //PCollection<Message> messageFromRetryTopic = pipeline.apply(String.format("Read %s messages from retry topic", messageType), SqsIO.read().withQueueUrl(messageConfig.pathToRetrySubscription()));

        //PCollection<Message> allMessages = PCollectionList.of(messageFromInputTopic).and(messageFromRetryTopic).apply(String.format("Combine %s input and retry messages", messageType), Flatten.pCollections());
        PCollection<Message> allMessages = PCollectionList.of(messageFromInputTopic).apply(String.format("Combine %s input and retry messages", messageType), Flatten.pCollections());

        return allMessages.apply(String.format("Transform message to %s Dto.", messageType ), ParDo.of(new MapMessageToCsvDtoDoFn(messageConfig.clazz())));

    }


    private static MessageConfigMapper configureMessageMapping(Configuration configuration) {
        String projectName = configuration.getInputProject();
        return MessageConfigMapper.instance.put(CsvDto.class, PipelineTags.CSV_FOR_RETRY,  configuration.getQueueCsv());

    }
}
