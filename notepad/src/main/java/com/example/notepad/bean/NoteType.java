package com.example.notepad.bean;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daxiong on 2016/10/27.
 */

public class NoteType extends DataSupport {
    public static final int NOTE_TYPE_MEETING     = 0;
    public static final int NOTE_TYPE_MEMO        = 1;
    public static final int NOTE_TYPE_ANNIVERSARY = 2;
    public static final int NOTE_TYPE_TODOLIST    = 3;
    public static final int NOTE_TYPE_ALL         = 4;

    private int noteTypeId;

    private String noteTypeString;

    private List<Note> noteList = new ArrayList<Note>();

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    public int getNoteTypeId() {
        return noteTypeId;
    }

    public void setNoteTypeId(int noteTypeId) {
        this.noteTypeId = noteTypeId;
    }

    public String getNoteTypeString() {
        return noteTypeString;
    }

    public void setNoteTypeString(String noteTypeString) {
        this.noteTypeString = noteTypeString;
    }
}
