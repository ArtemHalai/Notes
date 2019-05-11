package com.example.notes.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.notes.R;
import com.example.notes.data.StoreProvider;
import com.example.notes.data.dto.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public boolean showFooter = true;
    private List<Note> list;
    private List<Note> mFilteredList;
    private AdapterCallBack callBack;
    private AddNotes callBackFooter;
    private static final int FOOTER_VIEW = 1;
    private Note lastRemoved;
    private int lastRemovedPosition = -1;

    public NotesListAdapter(List<Note> list, AdapterCallBack callBack, AddNotes callBackFooter) {
        this.list = list;
        this.mFilteredList = list;
        this.callBack = callBack;
        this.callBackFooter = callBackFooter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup recyclerView, int type) {
        View v;

        if (type == FOOTER_VIEW) {
            v = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.list_item_footer, recyclerView, false);
            FooterViewHolder vh = new FooterViewHolder(v);
            return vh;
        }

        v = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.my_row, recyclerView, false);

        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        try {
            if (viewHolder instanceof MyViewHolder) {
                MyViewHolder vh = (MyViewHolder) viewHolder;

                Note note = list.get(position);
                char[] chars = note.getContent().toCharArray();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < chars.length; i++) {
                    if (i < 100) {
                        stringBuilder.append(chars[i]);
                    }
                }

                vh.content.setText(stringBuilder.toString());
                long time = note.getTimeInMillis();
                String dateRes = StoreProvider.getInstance().getTime(time);
                vh.timeInMillis.setText(dateRes);
                vh.date.setText(note.getDate());
            } else if (viewHolder instanceof FooterViewHolder) {
                if (callBackFooter != null && showFooter) {
                    callBackFooter.addMore();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //removing
    public Note remove(int position) {
        lastRemoved = list.remove(position);
        lastRemovedPosition = position;
        notifyItemRemoved(position);
        return lastRemoved;
    }

    public void cancelLastRemove() {
        if (lastRemovedPosition >= 0 && lastRemoved != null) {
            list.add(lastRemovedPosition, lastRemoved);
            notifyItemInserted(lastRemovedPosition);
            lastRemovedPosition = -1;
            lastRemoved = null;
        }
    }

    //updating
    public void updateList(List<Note> notes) {
        list = new ArrayList<>();
        list.addAll(notes);
        notifyDataSetChanged();
    }

    public List<Note> getmFilteredList() {
        return mFilteredList;
    }

    public void addAll(List<Note> l) {
        mFilteredList.addAll(l);
        notifyDataSetChanged();
    }

    public void hideFooter() {
        showFooter = false;
    }

    public void clearList() {
        list = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        if (list == null || list.size() == 0) {
            return 0;
        }

        if (showFooter) {
            return list.size() + 1;
        }

        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == list.size()) {
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }


    // view holder
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView content;
        private TextView date;
        private TextView timeInMillis;
        private ImageView noteArrow;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.note_content);
            date = itemView.findViewById(R.id.note_date);
            timeInMillis = itemView.findViewById(R.id.note_timeinmillis);
            noteArrow = itemView.findViewById(R.id.note_arrow);

            content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (callBack != null) {
                callBack.onRowClick(getAdapterPosition(), list.get(getAdapterPosition()));
            }
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    // call back
    //===========================================================================
    public interface AdapterCallBack {
        void onRowClick(int position, Note note);
    }

    public interface AddNotes {
        void addMore();
    }
}
