package com.example.notepad.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by daxiong on 2016/10/27.
 */

public class Note extends DataSupport {

    private int noteId;

    private NoteType noteType;

    //20161030+
    private int typeId;

    //20161031+
    private String typeString;

    private String noteTitle;

    private String noteContent;

    private long lastEditorTime;

    private long createTime;

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastEditorTime() {
        return lastEditorTime;
    }

    public void setLastEditorTime(long lastEditorTime) {
        this.lastEditorTime = lastEditorTime;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }
}
