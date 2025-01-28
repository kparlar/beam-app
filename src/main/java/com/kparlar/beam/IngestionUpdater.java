package com.kparlar.beam;

import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class IngestionUpdater {

    /**
     * Entry point for the application, this is where the beam pipeline is setup
     * <p>
     *
     *
     * @param args possible arguments to this executable are defined in {@link Configuration}
     */
    public static void main(String[] args) {
        Configuration options = PipelineOptionsFactory
                .fromArgs(args)
                .withValidation()
                .as(Configuration.class);

        //encrypt password so it is not exposed in the console
        options.setDbPassword(options.getDbPassword());

        PipelineFactory.createPipeline(options).run();

    }
}
