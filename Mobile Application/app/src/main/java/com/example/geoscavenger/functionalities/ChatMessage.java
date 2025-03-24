package com.example.geoscavenger.functionalities;

public class ChatMessage {
    private String mId;
    private String mText;
    private String mName;
    private String mType;

    public ChatMessage(){}

    public ChatMessage(String text, String name, String type){
        mText = text;
        mName = name;
        mType = type;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }
}
