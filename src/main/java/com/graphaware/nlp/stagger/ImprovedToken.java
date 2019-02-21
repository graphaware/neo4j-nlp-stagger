package com.graphaware.nlp.stagger;

public class ImprovedToken {

    private final String value;

    private final String lemma;

    private final String pos;

    private final String ne;

    private final String taggingScheme;

    private final int offset;

    public ImprovedToken(String value, String lemma, String pos, String ne, String taggingScheme, int offset) {
        this.value = value;
        this.lemma = lemma;
        this.pos = pos;
        this.ne = ne;
        this.taggingScheme = taggingScheme;
        this.offset = offset;
    }

    public String getValue() {
        return value;
    }

    public String getLemma() {
        return lemma;
    }

    public String getPos() {
        return pos;
    }

    public String getNe() {
        return ne;
    }

    public String getTaggingScheme() {
        return taggingScheme;
    }

    public int getOffset() {
        return offset;
    }
}
