package com.graphaware.nlp.stagger;

import com.graphaware.nlp.domain.AnnotatedText;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class StaggerProcessorTest {

    @Test
    public void testStaggerProcessing() throws Exception {
        StaggerProcessor processor = new StaggerProcessor();
        String text = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("article.txt").toURI())));

        AnnotatedText annotatedText = processor.tagText(text);
        annotatedText.getSentences().forEach(sentence -> {
            sentence.getTagOccurrences().values().forEach(tagOccurrences -> {
                tagOccurrences.forEach(tagOccurrence -> {
                    System.out.println(tagOccurrence.getValue());
                    System.out.println(tagOccurrence.getNamedEntity());
                    System.out.println(tagOccurrence.getElement().getPos());
                });
            });
        });
    }
}
