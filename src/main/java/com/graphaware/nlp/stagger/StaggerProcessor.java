package com.graphaware.nlp.stagger;

import se.su.ling.stagger.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class StaggerProcessor {

    String lexiconFile = null;
    String trainFile = null;
    String devFile = null;
    String modelFile = null;
    ArrayList<Dictionary> posDictionaries = new ArrayList<Dictionary>();
    ArrayList<Embedding> posEmbeddings = new ArrayList<Embedding>();
    ArrayList<Dictionary> neDictionaries = new ArrayList<Dictionary>();
    ArrayList<Embedding> neEmbeddings = new ArrayList<Embedding>();
    int posBeamSize = 8;
    int neBeamSize = 4;
    String lang = null;
    boolean preserve = false;
    float embeddingSigma = 0.1f;
    boolean plainOutput = false;
    String fold = null;
    int maxPosIters = 16;
    int maxNEIters = 16;
    boolean extendLexicon = true;
    boolean hasNE = true;
    private String getModelPath() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("swedish.bin").toURI());

        return file.getPath();
    }

    public void tagText(String input) throws Exception {
        String modelFile = getModelPath();

        List<String> inputFiles = Arrays.asList(input);

        TaggedToken[][] inputSents = null;

        ObjectInputStream modelReader = new ObjectInputStream(
                new FileInputStream(modelFile));
        System.err.println( "Loading Stagger model ...");
        Tagger tagger = (Tagger) modelReader.readObject();
        String lang = tagger.getTaggedData().getLanguage();
        modelReader.close();

        // TODO: experimental feature, might remove later
        tagger.setExtendLexicon(extendLexicon);
        if(!hasNE) tagger.setHasNE(false);

        for(String inputFile : inputFiles) {
            if(!(inputFile.endsWith(".txt") ||
                    inputFile.endsWith(".txt.gz")))
            {
                inputSents = tagger.getTaggedData().readConll(
                        inputFile, null, true,
                        !inputFile.endsWith(".conll"));
                Evaluation eval = new Evaluation();
                int count=0;
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
                for(TaggedToken[] sent : inputSents) {
                    if (count % 100 == 0 )
                        System.err.print("Tagging sentence nr: "+
                                count + "\r" );
                    count++;
                    TaggedToken[] taggedSent =
                            tagger.tagSentence(sent, true, preserve);

                    eval.evaluate(taggedSent, sent);
                    tagger.getTaggedData().writeConllGold(
                            writer, taggedSent, sent, plainOutput);
                }
                writer.close();
                System.err.println( "Tagging sentence nr: "+count);
                System.err.println(
                        "POS accuracy: "+eval.posAccuracy()+
                                " ("+eval.posCorrect+" / "+
                                eval.posTotal+")");
                System.err.println(
                        "NE precision: "+eval.nePrecision());
                System.err.println(
                        "NE recall:    "+eval.neRecall());
                System.err.println(
                        "NE F-score:   "+eval.neFscore());
            } else {
                String fileID =
                        (new File(inputFile)).getName().split(
                                "\\.")[0];
                BufferedReader reader = openUTF8File(inputFile);
                BufferedWriter writer = null;
                if(inputFiles.size() > 1) {
                    String outputFile = inputFile +
                            (plainOutput? ".plain" : ".conll");
                    writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(
                                            outputFile), "UTF-8"));
                } else {
                    writer = new BufferedWriter(
                            new OutputStreamWriter(System.out, "UTF-8"));
                }
                Tokenizer tokenizer = getTokenizer(reader, lang);
                ArrayList<Token> sentence;
                int sentIdx = 0;
                long base = 0;
                while((sentence=tokenizer.readSentence())!=null) {
                    TaggedToken[] sent =
                            new TaggedToken[sentence.size()];
                    if(tokenizer.sentID != null) {
                        if(!fileID.equals(tokenizer.sentID)) {
                            fileID = tokenizer.sentID;
                            sentIdx = 0;
                        }
                    }
                    for(int j=0; j<sentence.size(); j++) {
                        Token tok = sentence.get(j);
                        String id;
                        id = fileID + ":" + sentIdx + ":" +
                                tok.offset;
                        sent[j] = new TaggedToken(tok, id);
                    }
                    TaggedToken[] taggedSent =
                            tagger.tagSentence(sent, true, false);
                    tagger.getTaggedData().writeConllSentence(
                            (writer == null)? System.out : writer,
                            taggedSent, plainOutput);
                    sentIdx++;
                }
                tokenizer.yyclose();
                if(writer != null) writer.close();
            }
        }
    }

    private static BufferedReader openUTF8File(String name)
            throws IOException {
        if(name.equals("-"))
            return new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
        else if(name.endsWith(".gz"))
            return new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(
                            new FileInputStream(name)), StandardCharsets.UTF_8));
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(name), StandardCharsets.UTF_8));
    }

    private static Tokenizer getTokenizer(Reader reader, String lang) {
        Tokenizer tokenizer;
        if(lang.equals("sv")) {
            tokenizer = new SwedishTokenizer(reader);
        } else if(lang.equals("en")) {
            tokenizer = new EnglishTokenizer(reader);
        } else if(lang.equals("any")) {
            tokenizer = new LatinTokenizer(reader);
        } else {
            throw new IllegalArgumentException();
        }
        return tokenizer;
    }
}
