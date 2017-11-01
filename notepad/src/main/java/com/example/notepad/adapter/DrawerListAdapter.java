package com.example.notepad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.notepad.R;
import com.example.notepad.bean.NoteType;

import java.util.List;

/**
 * Created by daxiong on 2016/10/27.
 */

public class DrawerListAdapter extends BaseAdapter {

    private Context mContext;

    private List<NoteType> mNoteTypes;

    public DrawerListAdapter(Context context, List<NoteType> noteTypes) {
        this.mContext = context;
        this.mNoteTypes = noteTypes;
    }

    @Override
    public int getCount() {
        return mNoteTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return mNoteTypes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.drawer_list_item_layout, parent, false);
            holder = new Holder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.textView.setText(mNoteTypes.get(position).getNoteTypeString());
        return convertView;
    }

    public void refreshDrawerList(List<NoteType> newDataTypes) {
        mNoteTypes.clear();
        mNoteTypes.addAll(newDataTypes);
        notifyDataSetChanged();
    }

    static class Holder {
        TextView textView;
    }
}
