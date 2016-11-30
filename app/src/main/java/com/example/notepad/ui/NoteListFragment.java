package com.example.notepad.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.Explode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.notepad.R;
import com.example.notepad.adapter.NoteListAdapter;
import com.example.notepad.bean.Note;
import com.example.notepad.bean.NoteType;
import com.example.notepad.common.App;

import org.litepal.crud.DataSupport;

import java.util.Collections;
import java.util.List;

import static com.example.notepad.R.string.delete_note_success;

/**
 * Created by daxiong on 2016/10/27.
 */

public class NoteListFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static String NOTE_INIT_TYPE = "note_init_type";

    //public static String NOTE_LAYOUT_TYPE = "note_layout_type";

    public static String EDIT_NOTE_ID = "edit_note_id";

    private int currentNoteTypeId;

    private NoteType currentNoteType;

    private List<NoteType> currentNoteTypes;

    private List<Note> noteList;

    private NoteListAdapter noteListAdapter;

    private RecyclerView recycleView;

    private FloatingActionButton floatingActionButton;

    private SwipeRefreshLayout refreshLayout;

    private int lastVisibleItem;

    private LinearLayoutManager linearLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        currentNoteTypeId = bundle.getInt(NOTE_INIT_TYPE, 4);
        currentNoteType = (DataSupport.where("notetypeid = ?",
                String.valueOf(currentNoteTypeId)).find(NoteType.class, true)).get(0);
        currentNoteTypes = DataSupport.order("notetypeid").find(NoteType.class);
        if(currentNoteTypeId == 4){
            noteList = DataSupport.findAll(Note.class);
            List<NoteType> tempNoteTypeList = DataSupport.order("notetypeid").find(NoteType.class);;
            NoteType tempNoteType = null;
            try{
                tempNoteType = tempNoteTypeList.get(0);
                Log.e("1 Get type", tempNoteType.getNoteTypeString());
            }catch (Exception e){
                e.printStackTrace();
            }

        }else{
            noteList = currentNoteType.getNoteList();
        }

        reverseNoteList(noteList);

        noteListAdapter = new NoteListAdapter(getActivity(), noteList, currentNoteType.getNoteTypeString());
        noteListAdapter.setCurrentNoteTypeId(currentNoteTypeId);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_list_all, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.addNoteFloatingButton);
        floatingActionButton.setOnClickListener(this);
        recycleView = (RecyclerView) view.findViewById(R.id.notelist_recycleView);
        recycleView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = selectLayoutManager();
        linearLayoutManager = (LinearLayoutManager) layoutManager;
        recycleView.setAdapter(noteListAdapter);

        recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    floatingActionButton.setVisibility(View.VISIBLE);
                    if (lastVisibleItem + 1 == noteListAdapter.getItemCount()) {
                        ////此处实现上拉加载更多，更新noteListAdapter，从数据库中取下一页的数据出来
                        Log.d(App.TAG, "reload next page note");
                    }
                } else {
                    floatingActionButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresher);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        refreshLayout.setOnRefreshListener(this);
    }

    public static NoteListFragment newInstance(int noteTypeId, boolean isCardLayout) {
        Bundle bundle = new Bundle();
        bundle.putInt(NOTE_INIT_TYPE, noteTypeId);
        //20161031 17.01 删除
        //bundle.putBoolean(NOTE_LAYOUT_TYPE, isCardLayout);
        NoteListFragment noteListFragment = new NoteListFragment();
        noteListFragment.setArguments(bundle);
        return noteListFragment;
    }

    @Override
    public void onClick(View v) {
        editNote();
    }

    public void editNote(){
        Intent intent = new Intent(getActivity(), EditNoteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(NOTE_INIT_TYPE, currentNoteTypeId);
        bundle.putInt(EDIT_NOTE_ID, -1);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void reloadNoteListAsSelectNoteType(int selectNoteTypeId) {
        currentNoteTypeId = selectNoteTypeId;
        if(selectNoteTypeId == 4){
            NoteType selectNoteType = (DataSupport.where("notetypeid = ?",
                    String.valueOf(selectNoteTypeId)).find(NoteType.class, true)).get(0);
            List<Note> selectNoteList = DataSupport.findAll(Note.class);

//            NoteType selectNoteType = (DataSupport.where("notetypeid = ?",
//                    String.valueOf(selectNoteTypeId)).find(NoteType.class, true)).get(0);
//            List<Note> selectNoteList = selectNoteType.getNoteList();

            noteListAdapter.setCurrentNoteTypeId(selectNoteTypeId);
            noteListAdapter.setCurrentNoteTypeString(selectNoteType.getNoteTypeString());
            reverseNoteList(selectNoteList);
            noteListAdapter.setNoteList(selectNoteList);
        }else {
            NoteType selectNoteType = (DataSupport.where("notetypeid = ?",
                    String.valueOf(selectNoteTypeId)).find(NoteType.class, true)).get(0);
            List<Note> selectNoteList = selectNoteType.getNoteList();
            noteListAdapter.setCurrentNoteTypeId(selectNoteTypeId);
            noteListAdapter.setCurrentNoteTypeString(selectNoteType.getNoteTypeString());
            reverseNoteList(selectNoteList);
            noteListAdapter.setNoteList(selectNoteList);
        }
    }

    public void clearNoteList() {
        noteListAdapter.clearNoteList();
    }

    public void reloadNewestAddNote() {
        noteListAdapter.addNote(DataSupport.findLast(Note.class));
        recycleView.scrollToPosition(0);
    }

    public void reverseNoteList(List<Note> notes) {
        Collections.reverse(notes);
    }

    public RecyclerView.LayoutManager selectLayoutManager() {
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(layoutManager);
        return layoutManager;
    }

    public void setShowNoteListItem(boolean[] showItemIndicator) {
        noteListAdapter.setItemVisible(showItemIndicator);
    }

    public void setOrderNoteListType(int orderListIndicator) {
        noteListAdapter.orderNoteList(orderListIndicator);
    }

    public void hideFloatActionBut() {
        floatingActionButton.setVisibility(View.GONE);
    }

    public void showFloatActionBut() {
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        reloadNoteListAsSelectNoteType(currentNoteTypeId);
        refreshLayout.setRefreshing(false);
    }
}
