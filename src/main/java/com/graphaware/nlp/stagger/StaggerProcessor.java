package com.graphaware.nlp.stagger;

import com.graphaware.nlp.domain.AnnotatedText;
import com.graphaware.nlp.domain.Sentence;
import com.graphaware.nlp.domain.Tag;
import se.su.ling.stagger.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class StaggerProcessor {

    private static final String BACKGROUND_SYMBOL = "O";
    private static final String SWEDISH_LANGUAGE_CODE = "sv";
    private static final boolean extendLexicon = true;
    private static final boolean hasNE = true;

    private final Tagger tagger;

    public StaggerProcessor() {
        try {
            System.out.println( "Loading Stagger model ...");
            ObjectInputStream modelReader = new ObjectInputStream(new FileInputStream(getModelPath()));
            tagger = (Tagger) modelReader.readObject();
            modelReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getModelPath() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("swedish.bin").toURI());

        return file.getPath();
    }

    public AnnotatedText tagText(String text) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        String lang = tagger.getTaggedData().getLanguage();

        // TODO: experimental feature, might remove later
        tagger.setExtendLexicon(extendLexicon);
        if (!hasNE) {
            tagger.setHasNE(false);
        }

        String fileID = UUID.randomUUID().toString();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        Tokenizer tokenizer = getTokenizer(reader, lang);
        ArrayList<Token> sent;
        int sentIdx = 0;
        long base = 0;

        List<ArrayList<Token>> sentences = new ArrayList<>();
        AnnotatedText annotatedText = new AnnotatedText();
        annotatedText.setText(text);

        while( (sent = tokenizer.readSentence() ) != null ) {
            sentences.add(sent);
        }

        int sentencesCount = 0;
        for (ArrayList<Token> s : sentences) {
            String sentenceString = String.join(" ", s.stream().map(t -> {return t.value;}).collect(Collectors.toList()));
            TaggedToken[] sentence = new TaggedToken[s.size()];
            if(tokenizer.sentID != null) {
                if(!fileID.equals(tokenizer.sentID)) {
                    fileID = tokenizer.sentID;
                    sentIdx = 0;
                }
            }

            for(int j=0; j < s.size(); j++) {
                Token tok = s.get(j);
                String id = fileID + ":" + sentIdx + ":" + tok.offset;
                sentence[j] = new TaggedToken(tok, id);
            }

            TaggedToken[] taggedSent = tagger.tagSentence(sentence, true, false);
            annotatedText.getSentences().add(processSentence(taggedSent, tagger, sentencesCount, sentenceString));
            sentencesCount++;
        }

        tokenizer.yyclose();

        return annotatedText;
    }

    private Sentence processSentence(TaggedToken[] sentence, Tagger tagger, int sentenceNumber, String sentenceString) throws Exception {
        Sentence s = new Sentence(sentenceString, sentenceNumber);
        TaggedToken[] taggedSentence = tagger.tagSentence(sentence, true, false);
        List<ImprovedToken> tokens = new ArrayList<>();
        for (TaggedToken token : taggedSentence) {
            tokens.add(tagger.getTaggedData().getImprovedToken(token));
        }
        addTokensToSentence(s, tokens);

        return s;
    }

    private void addTokensToSentence(Sentence s, List<ImprovedToken> tokens) {
        TokenHolder currentToken = new TokenHolder();
        currentToken.setPos("");
        currentToken.setNe(BACKGROUND_SYMBOL);

        for (ImprovedToken token : tokens) {

            if (token.getTaggingScheme().equals(BACKGROUND_SYMBOL)) {
                s.addTagOccurrence(token.getOffset(),token.getOffset() + token.getValue().length(), token.getValue(), getTag(token.getValue(), token.getLemma(), token.getNe(), token.getPos()));
                continue;
            }

            if (token.getTaggingScheme().equals("B")) {
                currentToken.updateToken(token.getLemma(), token.getValue());
                currentToken.setBeginPosition(token.getOffset());
                currentToken.setNe(token.getNe());
                currentToken.setPos(token.getPos());

                continue;
            }

            if (token.getTaggingScheme().equals("I") && currentToken.getNe().equals(token.getNe())) {
                currentToken.updateToken(token.getLemma(), token.getValue());
                Tag tag = getTag(currentToken.getOriginalValue(), currentToken.getToken(), currentToken.getNe(), currentToken.getPos());
                s.addTagOccurrence(currentToken.getBeginPosition(), currentToken.getBeginPosition() + currentToken.getToken().length(), currentToken.getOriginalValue(), tag);
                currentToken.reset();
            }
        }
    }

    private Tag getTag(String value, String lemma, String ne, String pos) {
        Tag tag = new Tag(lemma, SWEDISH_LANGUAGE_CODE, value);
        tag.setPos(Arrays.asList(pos));
        tag.setNe(Arrays.asList(ne));

        return tag;
    }

    class TokenHolder {

        private String ne;
        private String pos;
        private StringBuilder sb;
        private StringBuilder sbOriginalValue;
        private int beginPosition;
        private int endPosition;
        private List<String> tokenIds = new ArrayList<>();

        public TokenHolder() {
            reset();
        }

        public String getNe() {
            return ne;
        }

        public String getToken() {
            return sb.toString();
        }

        public String getOriginalValue() {
            return sbOriginalValue.toString();
        }

        public int getBeginPosition() {
            return beginPosition;
        }

        public int getEndPosition() {
            return endPosition;
        }

        public void setNe(String ne) {
            this.ne = ne;
        }

        public void updateToken(String tknStr, String originalValue) {
            if (this.getToken().length() > 0) {
                this.sb.append(" ");
                this.sbOriginalValue.append(" ");
            }
            this.sb.append(tknStr);
            this.sbOriginalValue.append(originalValue);
        }

        public void updateTokenAndTokenId(String tknStr, String originalValue, String tokenId) {
            updateToken(tknStr, originalValue);
            tokenIds.add(tokenId);
        }

        public List<String> getTokenIds() {
            return tokenIds;
        }

        public void setBeginPosition(int beginPosition) {
            if (this.beginPosition < 0) {
                this.beginPosition = beginPosition;
            }
        }

        public void setEndPosition(int endPosition) {
            this.endPosition = endPosition;
        }

        public final void reset() {
            sb = new StringBuilder();
            sbOriginalValue = new StringBuilder();
            beginPosition = -1;
            endPosition = -1;
            tokenIds.clear();
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }
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
