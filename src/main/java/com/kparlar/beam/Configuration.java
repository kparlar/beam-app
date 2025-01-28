package com.kparlar.beam;


import org.apache.beam.sdk.io.aws.options.AwsOptions;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.Validation;

public interface Configuration extends AwsOptions {
    /**
     * Specify the input project for messages by supplying the --inputProject= argument.
     *
     */
    @Description("The project to consume messages")
    String getInputProject();

    void setInputProject(String inputProject);

    // --- begin: Csv ---

    @Description("Name of the Csv Queue")
    @Validation.Required
    String getQueueCsv();

    void setQueueCsv(String csvSubscription);
    // --- end: Csv ---

    @Description("The project to send map messages to")
    @Validation.Required
    String getOutputProject();

    void setOutputProject(String outputProject);

    @Description("The database URL")
    @Validation.Required
    String getDbUrl();

    void setDbUrl(String dbUrl);

    @Description("The database user")
    @Validation.Required
    String getDbUser();

    void setDbUser(String dbUser);

    @Description("The database password")
    @Validation.Required
    String getDbPassword();

    void setDbPassword(String dbPassword);
}
