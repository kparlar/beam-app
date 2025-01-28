package com.kparlar.beam.transform;

import com.kparlar.beam.PipelineTags;
import com.kparlar.beam.dto.MessageDto;
import com.kparlar.beam.function.persist.PersistMessageFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTagList;

public class PersistMessageTransform extends PTransform<PCollection<MessageDto>, PCollectionTuple> {


    @Override
    public PCollectionTuple expand(PCollection<MessageDto> input) {
        return input.apply(ParDo.of(new PersistMessageFn()).withOutputTags(PipelineTags.STATUS_COMPLETED, TupleTagList.of(PipelineTags.STATUS_INITIAL_UPLOAD)));
    }
}
