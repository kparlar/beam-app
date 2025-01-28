package com.kparlar.beam.function.persist;

import com.google.gson.Gson;
import com.kparlar.beam.Configuration;
import com.kparlar.beam.PipelineTags;
import com.kparlar.beam.dto.CsvDto;
import com.kparlar.beam.dto.MessageDto;
import com.kparlar.beam.dto.FileDto;
import com.kparlar.beam.entity.FileEntity;
import com.kparlar.beam.service.FileService;
import com.kparlar.beam.util.DBConnectionUtil;

import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PersistMessageFn extends DoFn<MessageDto, CsvDto> {
    Logger logger = LoggerFactory.getLogger(PersistMessageFn.class);

    private static Configuration configuration;
    private static Gson gson = new Gson();
    private static FileService fileService = new FileService();
    @StartBundle
    public void startBundle(StartBundleContext c) {
        configuration = c.getPipelineOptions().as(Configuration.class);
        DBConnectionUtil.init(configuration);
    }
    @ProcessElement
    public void processElement(ProcessContext c) {
        CsvDto csvDto;
        try{
        MessageDto messageDto = c.element();
        FileDto fileDto = gson.fromJson(messageDto.getJson(), messageDto.getClazz());
        csvDto = (CsvDto) fileDto;
      /*Date publishedDate =
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH)
              .parse(csvDto.getPublishedDate());*/
        FileEntity fileEntity = FileEntity.builder()
                .fileName(csvDto.getFileName())
                        .id(csvDto.getId())
                                .publishedDate(null)
                                        .status(csvDto.getStatus())
                                                .fileName(csvDto.getFileName()).build();

         fileService.upsert(fileEntity);
         c.output(PipelineTags.STATUS_COMPLETED, csvDto);


        logger.info("MessageId:{}",messageDto.getReceiptHandler());
         }catch (Exception e){
        logger.error("Error occured on Persis Operation {}: ",e.getMessage());

        }
    }

    private void outputAsRetryOrError(){

    }
}
