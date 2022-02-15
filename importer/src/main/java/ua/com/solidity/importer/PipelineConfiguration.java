package ua.com.solidity.importer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.common.prototypes.*;
import ua.com.solidity.importer.pipeline.ImportRevisionGroupRowImporter;
import ua.com.solidity.pipeline.ContextPipelinePrototypeProvider;
import ua.com.solidity.pipeline.PipelineFactory;

@Configuration
public class PipelineConfiguration {
    @Bean
    public PipelineFactory importerFactory(ApplicationContext context) {
        return new PipelineFactory(new ContextPipelinePrototypeProvider(context, "pp", ""));
    }

    @Bean
    public PPInputStream ppInputStream() {
        return new PPInputStream();
    }

    @Bean
    public PPZipFile ppZipFile() { return new PPZipFile(); }

    @Bean
    public PPZipInflaterStream ppZipInflaterStream() {
        return new PPZipInflaterStream();
    }

    @Bean
    public PPCSVParser ppCSVParser() {
        return new PPCSVParser();
    }

    @Bean
    public PPXMLParser ppXMLParser() {
        return new PPXMLParser();
    }

    @Bean PPNoParser ppNoParser() {
        return new PPNoParser();
    }

    @Bean
    public PPJSONParser ppJSONParser() { return new PPJSONParser(); }

    @Bean
    public PPXLSParser ppXLSParser() { return new PPXLSParser(); }

    @Bean
    public PPDictionary ppDictionary() {
        return new PPDictionary();
    }

    @Bean
    public ImportRevisionGroupRowImporter ppRowImporter() {
        return new ImportRevisionGroupRowImporter();
    }

    @Bean
    public PPRevisionExtension ppRevisionExtension() {
        return new PPRevisionExtension();
    }

    @Bean
    public PPArrayExtractor ppArrayExtractor() {
        return new PPArrayExtractor();
    }

    @Bean PPDBTableWriter ppTableWriter() {
        return new PPDBTableWriter();
    }
}
