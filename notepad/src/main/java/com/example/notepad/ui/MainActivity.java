package com.example.notepad.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.notepad.R;
import com.example.notepad.adapter.DrawerListAdapter;
import com.example.notepad.bean.Note;
import com.example.notepad.bean.NoteType;
import com.example.notepad.common.DoubleClickExitHelper;
import com.example.notepad.common.NoteUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static com.example.notepad.ui.NoteListFragment.EDIT_NOTE_ID;
import static com.example.notepad.ui.NoteListFragment.NOTE_INIT_TYPE;

//import android.support.v7.app.AlertDialog;

/**
 * Created by daxiong on 2016/10/27.
 */

public class MainActivity extends AppCompatActivity {

    public static final String CURRENT_NOTE_MENU_KEY = "current_note_menu_key";

    public static final String PREFERENCE_FILE_NAME = "note.settings";
    //使用注解省去大量findViewById和setOnClickListener
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.drawerlayout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.drawer_listview)
    ListView drawerListView;
    @InjectView(R.id.drawer_menu)
    View drawerRootView;
    @InjectView(R.id.exit_app)
    Button exitAppBut;
    /**
     * 记事本分类
     */
    private List<NoteType> noteTypeList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerListAdapter drawerListAdapter;
    /**
     * 当前记事本类型id，默认4 表示所有类型
     */
    private int mCurrentType = NoteType.NOTE_TYPE_ALL;
    /**
     * 查看选项
     */
    private boolean showItemIndicator[] = {true, true, true};
    /**
     * 排序方式
     */
    private int orderListIndicator = 1;

    private SharedPreferences sharedPreferences;

    private NoteListFragment noteListFragment;

    /**
     * 添加记事本事件
     */
    private boolean hasAddedNote = false;

    /**
     * 删除记事本事件
     */
    private boolean hasClearedNotes = false;

    private boolean noteRestoreDefaultEvent = false;

    /**
     * 刷新记事本事件
     */
    private boolean noteRefreshNoteTypeEvent = false;
    /**
     * 当前记事本名称
     */
    private String currentNoteTypeString;

    private DoubleClickExitHelper mDoubleClickExitHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            mCurrentType = savedInstanceState.getInt(CURRENT_NOTE_MENU_KEY);
        }
        ButterKnife.inject(this);
        EventBus.getDefault().register(this);
        mDoubleClickExitHelper = new DoubleClickExitHelper(this);
        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        initToolBar();
        initDrawListView();
        initRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        noteListFragment.selectLayoutManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasAddedNote) {
            noteListFragment.reloadNewestAddNote();
            hasAddedNote = false;
        }
        if (hasClearedNotes) {
            noteListFragment.clearNoteList();
            hasClearedNotes = false;
        }
        if (noteRestoreDefaultEvent) {
            drawerListAdapter.refreshDrawerList(initNoteType());
            noteListFragment.clearNoteList();
            noteRestoreDefaultEvent = false;
        }
        if (noteRefreshNoteTypeEvent) {
            drawerListAdapter.refreshDrawerList(initNoteType());
            noteRefreshNoteTypeEvent = false;
        }
    }

    /**
     * 初始化记事本类型列表，如果应用是第一次安装则新建列表，
     * 否则从数据库中读出列表。
     *
     * @return 记事本类型列表
     */
    public List<NoteType> initNoteType() {
        List<NoteType> noteTypes;
        // 第一次安装时在notetype表中增加默认的四种类型
        if (sharedPreferences.getBoolean(getString(R.string.first_init_app_key), true)) {
            noteTypes = new ArrayList<NoteType>();
            String[] defaultNoteTypeList = getResources().getStringArray(R.array.default_notetype_list);
            for (int i = 0; i < defaultNoteTypeList.length; i++) {
                NoteType noteType = new NoteType();
                noteType.setNoteTypeId(i);
                noteType.setNoteTypeString(defaultNoteTypeList[i]);
                noteType.save();
                noteTypes.add(noteType);
            }
            sharedPreferences.edit().putBoolean(getString(R.string.first_init_app_key), false).apply();
        } else {
            // 非第一次安装从数据库中取出数据
            noteTypes = DataSupport.order("notetypeid").find(NoteType.class);
        }
        return noteTypes;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //mDrawerToggle.syncState();
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openOrCloseDrawer();
                }
            });
        }
    }

    /**
     * 控制侧滑栏的开合
     */
    private void openOrCloseDrawer() {
        if (drawerLayout.isDrawerOpen(drawerRootView)) {
            drawerLayout.closeDrawer(drawerRootView);
        } else {
            drawerLayout.openDrawer(drawerRootView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_NOTE_MENU_KEY, mCurrentType);
    }

    /**
     * 初始化顶部工具条
     */
    private void initToolBar() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }

    /**
     * 初始化侧滑栏
     */
    private void initDrawListView() {
        noteTypeList = initNoteType();
        currentNoteTypeString = noteTypeList.get(mCurrentType).getNoteTypeString();
        toolbar.setTitle(currentNoteTypeString);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                toolbar.setTitle(getResources().getString(R.string.toolbar_title));
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                toolbar.setTitle(currentNoteTypeString);
            }
        };
        mDrawerToggle.syncState();
        //设置监听器
        drawerLayout.addDrawerListener(mDrawerToggle);
        drawerListAdapter = new DrawerListAdapter(this, noteTypeList);
        //设置适配器
        drawerListView.setAdapter(drawerListAdapter);
        drawerListView.setItemChecked(mCurrentType, true);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawer(drawerRootView);
                if (mCurrentType != position) {
                    currentNoteTypeString = noteTypeList.get(position).getNoteTypeString();
                    toolbar.setTitle(currentNoteTypeString);
                    drawerListView.setItemChecked(position, true);
                    changeToSelectNoteType(position);
                    //change current noteType id
                    mCurrentType = position;
                }
            }
        });
    }

    //转至指定类型记事本
    private void changeToSelectNoteType(int selectNoteTypeItem) {
        //checkNoteListFragment();
        noteListFragment.reloadNoteListAsSelectNoteType(selectNoteTypeItem);
    }

    /**
     * 初始化循环列表
     */
    private void initRecyclerView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        noteListFragment = NoteListFragment.newInstance(mCurrentType, false);
        fragmentManager.beginTransaction().replace(R.id.drawer_content, noteListFragment, null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
//        ComponentName componentName = getComponentName();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_addNote:
                addNote();
                break;
            case R.id.action_orderItem:
                orderNoteList();
                break;
            case R.id.action_showItem:
                setNoteListShowItem();
                break;
            case R.id.action_cleanNoteItem:
                deleteAllNotes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 新建记事本
     */
    private void addNote() {
        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
        intent.putExtra(NOTE_INIT_TYPE, mCurrentType);
        intent.putExtra(EDIT_NOTE_ID, -1);
        startActivity(intent);
    }

    /**
     * 记事本排序方式
     */
    private void orderNoteList() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_order_type_note_list));
        String orderItem[] = {getString(R.string.order_createTime_item), getString(R.string.order_laseEdit_item), getString(R.string.order_title_item)};
        builder.setSingleChoiceItems(orderItem, orderListIndicator, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                orderListIndicator = which;
            }
        });
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        noteListFragment.setOrderNoteListType(orderListIndicator);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
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

    /**
     * 记事本查看选项
     */
    public void setNoteListShowItem() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_show_note_list_item));
        String showItem[] = {getString(R.string.note_list_noteType_item), getString(R.string.note_list_createTime_item), getString(R.string.note_list_lastEditorTime_item)};
        builder.setMultiChoiceItems(showItem, showItemIndicator, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                showItemIndicator[which] = isChecked;
            }
        });
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        noteListFragment.setShowNoteListItem(showItemIndicator);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
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

    /**
     * 全部删除
     */
    private void deleteAllNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_allnote_hint);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
//                        NoteType clearNoteType = (DataSupport.where("notetypeid = ?",
//                                String.valueOf(mCurrentType)).find(NoteType.class, true)).get(0);
//                        List<Note> clearNoteList = clearNoteType.getNoteList();
                        List<NoteType> clearNoteTypeList = new ArrayList<>();
                        if (mCurrentType != 4) {
                            NoteType temp = (DataSupport.where("notetypeid = ?",
                                    String.valueOf(mCurrentType)).find(NoteType.class, true)).get(0);
                            clearNoteTypeList.add(temp);
                        } else {
                            for (int i = 0; i < 4; i++) {
                                NoteType temp = (DataSupport.where("notetypeid = ?",
                                        String.valueOf(i)).find(NoteType.class, true)).get(0);
                                clearNoteTypeList.add(temp);
                            }
                        }
                        Log.e("clearNoteTypeList size", String.valueOf(clearNoteTypeList.size()));

                        List<Note> clearNoteList = new ArrayList<>();
                        for (NoteType noteType : clearNoteTypeList) {
                            try {
                                clearNoteList.addAll(noteType.getNoteList());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Log.e("clearNoteList size", String.valueOf(clearNoteList.size()));
                        for (Note note : clearNoteList) {
                            note.delete();
                        }
                        noteListFragment.clearNoteList();
                        Toast.makeText(MainActivity.this, getString(R.string.delete_note_success),
                                Toast.LENGTH_SHORT).show();
                        NotepadWidget.actionToNotepadWidget(MainActivity.this);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
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

    public void onEvent(Integer event) {
        switch (event) {
            case NoteUtils.NOTE_ADD_EVENT:
                hasAddedNote = true;
                break;
            case NoteUtils.NOTE_UPDATE_EVENT:
                changeToSelectNoteType(mCurrentType);
                break;
            case NoteUtils.NOTE_CLEARALL_EVENT:
                hasClearedNotes = true;
                break;
            case NoteUtils.NOTE_RESTORY_DEFAULT_EVENT:
                noteRestoreDefaultEvent = true;
                break;
            case NoteUtils.NOTE_TYPE_UPDATE_EVENT:
                noteRefreshNoteTypeEvent = true;
                break;
            case NoteUtils.CHANGE_THEME_EVENT:
                this.recreate();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  unregister EventBus
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.exit_app)
    public void onExitApp() {
        this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(drawerRootView)) {
                drawerLayout.closeDrawer(drawerRootView);
                return true;
            }
            return mDoubleClickExitHelper.onKeyDown(keyCode, event);
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (drawerLayout.isDrawerOpen(drawerRootView)) {
                drawerLayout.closeDrawer(drawerRootView);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
