package com.graphaware.nlp.stagger;

import org.junit.Test;

public class StaggerProcessorTest {

    @Test
    public void testStaggerProcessing() throws Exception {
        StaggerProcessor processor = new StaggerProcessor();
        String file = "test.txt";

        processor.tagText(file);
    }
}
