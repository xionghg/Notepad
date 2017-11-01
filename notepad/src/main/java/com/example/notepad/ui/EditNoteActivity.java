package com.example.notepad.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.notepad.R;
import com.example.notepad.bean.Note;
import com.example.notepad.bean.NoteType;
import com.example.notepad.common.NoteUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by daxiong on 2016/10/27.
 */

public class EditNoteActivity extends ToolbarActivity {

    @InjectView(R.id.title_edit_text)
    MaterialEditText noteTitleEditText;

    @InjectView(R.id.content_edit_text)
    MaterialEditText noteContentEditText;

    @InjectView(R.id.create_time_line_text)
    TextView createTimeLine;

    @InjectView(R.id.last_editor_time_line_text)
    TextView lastEditorTimeLine;

    @InjectView(R.id.note_type_spinner)
    Spinner noteTypeSpinner;

    private MenuItem doneMenuItem;

    private int editNoteID;

    private int currentNoteTypeId = 0;

    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        currentNoteTypeId = intent.getIntExtra(NoteListFragment.NOTE_INIT_TYPE, 0);
        if (currentNoteTypeId == 4) {
            currentNoteTypeId = 0;
        }
        editNoteID = intent.getIntExtra(NoteListFragment.EDIT_NOTE_ID, -1);
        if (editNoteID == -1) {
            currentNote = null;
        } else {
            currentNote = DataSupport.where("noteid=?", String.valueOf(editNoteID)).find(Note.class).get(0);
        }
        super.onCreate(savedInstanceState);
        initNote();
    }

    private void initNote() {
        List<NoteType> noteTypes = DataSupport.order("notetypeid").find(NoteType.class);
        int arraySize = noteTypes.size() - 1;
        String[] noteTypeArray = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            noteTypeArray[i] = noteTypes.get(i).getNoteTypeString();
        }

        noteTypeSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.simple_spinner_item, noteTypeArray));
        noteTypeSpinner.setSelection(currentNoteTypeId);
        noteTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentNoteTypeId = position;
                if (position != getIntent().getExtras().getInt(NoteListFragment.NOTE_INIT_TYPE, 0)) {
                    String labelSrc = noteTitleEditText.getText().toString();
                    String contentSrc = noteContentEditText.getText().toString();
                    if (TextUtils.isEmpty(labelSrc) && TextUtils.isEmpty(contentSrc)) {
                        doneMenuItem.setVisible(false);
                    } else {
                        doneMenuItem.setVisible(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (currentNote == null) {
            createTimeLine.setVisibility(View.GONE);
            lastEditorTimeLine.setVisibility(View.GONE);
        } else {
            noteTitleEditText.requestFocus();
            noteTitleEditText.setText(currentNote.getNoteTitle());
            noteContentEditText.setText(currentNote.getNoteContent());
            createTimeLine.setText(getString(R.string.create_time_line_default)
                    + NoteUtils.changeToDefaultTimeFormat(currentNote.getCreateTime()));
            lastEditorTimeLine.setText(getString(R.string.editor_time_line_default)
                    + NoteUtils.changeToDefaultTimeFormat(currentNote.getLastEditorTime()));
        }
        noteTitleEditText.addTextChangedListener(new SimpleTextWatcher());
        noteContentEditText.addTextChangedListener(new SimpleTextWatcher());
    }

    @Override
    protected String getToolbarTitle() {
        String toolbarTitle;
        if (currentNote == null) {
            toolbarTitle = getResources().getString(R.string.edit_note_type_add);
        } else {
            toolbarTitle = getResources().getString(R.string.edit_note_type_edit);
        }
        return toolbarTitle;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_edit_note;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        doneMenuItem = menu.getItem(0);
        doneMenuItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.done) {
            saveNote();
            return true;
        } else if (id == android.R.id.home) {
            if (doneMenuItem.isVisible()) {
                showNotSaveNoteDialog();
                return true;
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (doneMenuItem != null && doneMenuItem.isVisible()) {
                showNotSaveNoteDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showNotSaveNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.not_save_note_leave_tip);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        saveNote();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        EditNoteActivity.this.finish();
                        break;
                    default:
                        break;
                }
            }
        };
        builder.setPositiveButton(R.string.sure, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.show();
    }

    private void saveNote() {
        //hideKeyBoard(noteTitleEditText);
        NoteType noteType = (DataSupport.where("notetypeid=?", String.valueOf(currentNoteTypeId)).find(NoteType.class, true)).get(0);
        if (currentNote == null) {
            int lastNoteId;
            if (DataSupport.findLast(Note.class) == null) {
                lastNoteId = 0;
            } else {
                lastNoteId = DataSupport.findLast(Note.class).getNoteId();
            }
            int noteId = lastNoteId + 1;

            Note note = new Note();
            note.setNoteType(noteType);
            //
            note.setTypeId(currentNoteTypeId);
            note.setTypeString(noteType.getNoteTypeString());
            Log.e("新建Note，NoteId", String.valueOf(noteId));
            Log.e("NoteTypeId", String.valueOf(noteType.getNoteTypeId()));
            note.setNoteId(noteId);
            note.setNoteTitle(noteTitleEditText.getText().toString());
            note.setNoteContent(noteContentEditText.getText().toString());
            //String currentTime = NoteUtils.changeToDefaultTimeFormat(System.currentTimeMillis());
            note.setCreateTime(System.currentTimeMillis());
            note.setLastEditorTime(System.currentTimeMillis());
            note.save();
        } else {
            if (currentNote.isSaved()) {
                currentNote.setNoteTitle(noteTitleEditText.getText().toString());
                currentNote.setNoteContent(noteContentEditText.getText().toString());
                currentNote.setLastEditorTime(System.currentTimeMillis());
                currentNote.setNoteType(noteType);
                //
                currentNote.setTypeId(currentNoteTypeId);
                currentNote.setTypeString(noteType.getNoteTypeString());
                currentNote.save();
            }
        }
        if (currentNoteTypeId == getIntent().getExtras().getInt(NoteListFragment.NOTE_INIT_TYPE, 0)) {
            if (currentNote == null) {
                EventBus.getDefault().post(NoteUtils.NOTE_ADD_EVENT);
            } else {
                EventBus.getDefault().post(NoteUtils.NOTE_UPDATE_EVENT);
            }
        } else {
            EventBus.getDefault().post(NoteUtils.NOTE_UPDATE_EVENT);
        }

        NotepadWidget.actionToNotepadWidget(this);
        finish();
    }

    class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (doneMenuItem == null)
                return;
            String labelSrc = noteTitleEditText.getText().toString();
            String contentSrc = noteContentEditText.getText().toString();
            /*String label = labelSrc.replaceAll("\\s*|\t|\r|\n", "");
            String content = contentSrc.replaceAll("\\s*|\t|\r|\n", "");*/
            if (!TextUtils.isEmpty(labelSrc) && !TextUtils.isEmpty(contentSrc)) {
                if (currentNote == null) {
                    doneMenuItem.setVisible(true);
                } else {
                    //未修改时保存按键不可见
                    if (TextUtils.equals(labelSrc, currentNote.getNoteTitle()) && TextUtils.equals(contentSrc, currentNote.getNoteContent())) {
                        doneMenuItem.setVisible(false);
                    } else {
                        doneMenuItem.setVisible(true);
                    }
                }
            } else {
                doneMenuItem.setVisible(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    /*private void hideKeyBoard(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }*/
}
