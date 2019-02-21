package com.graphaware.nlp.stagger;

import com.graphaware.nlp.domain.AnnotatedText;
import com.graphaware.nlp.domain.Tag;
import com.graphaware.nlp.dsl.request.PipelineSpecification;
import com.graphaware.nlp.processor.TextProcessor;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StaggerTextProcessor implements TextProcessor {

    @Override
    public void init() {

    }

    @Override
    public String getAlias() {
        return null;
    }

    @Override
    public List<String> getPipelines() {
        return null;
    }

    @Override
    public void createPipeline(PipelineSpecification pipelineSpecification) {

    }

    @Override
    public boolean checkPipeline(String name) {
        return false;
    }

    @Override
    public AnnotatedText annotateText(String text, PipelineSpecification pipelineSpecification) {
        return null;
    }

    @Override
    public Tag annotateSentence(String text, PipelineSpecification pipelineSpecification) {
        return null;
    }

    @Override
    public Tag annotateTag(String text, PipelineSpecification pipelineSpecification) {
        return null;
    }

    @Override
    public List<Tag> annotateTags(String text, String lang) {
        return null;
    }

    @Override
    public List<Tag> annotateTags(String text, PipelineSpecification pipelineSpecification) {
        return null;
    }

    @Override
    public boolean checkLemmaIsValid(String value) {
        return false;
    }

    @Override
    public AnnotatedText sentiment(AnnotatedText annotatedText) {
        return null;
    }

    @Override
    public void removePipeline(String pipeline) {

    }

    @Override
    public String train(String alg, String modelId, String file, String lang, Map<String, Object> params) {
        return null;
    }

    @Override
    public String test(String alg, String modelId, String file, String lang) {
        return null;
    }

    private String getModelFilePath() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("swedish.bin").toURI());

        return file.getPath();
    }
}
