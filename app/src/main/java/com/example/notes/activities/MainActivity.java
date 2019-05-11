package com.example.notes.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.adapters.NotesListAdapter;
import com.example.notes.data.DataBaseHandler;
import com.example.notes.data.dto.Note;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnClickListener, NotesListAdapter.AdapterCallBack, NotesListAdapter.AddNotes, SwipeRefreshLayout.OnRefreshListener {

    private Toolbar toolbar;
    private SearchView searchView;
    private ImageView addButton;
    private ProgressBar progressBar;
    private DataBaseHandler dataBaseHandler;
    private RecyclerView notesList;
    private List<Note> list;
    private NotesListAdapter adapter;
    private Gson gson;
    private int count = 0;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataBaseHandler = new DataBaseHandler(this);
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.search_field);
        addButton = findViewById(R.id.add_button);
        progressBar = findViewById(R.id.progress_bar);
        notesList = findViewById(R.id.notes_list);
        swipeRefreshLayout = findViewById(R.id.swipe_update);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorForDetails));
        swipeRefreshLayout.setOnRefreshListener(this);

        addButton.setOnClickListener(this);
        searchView.setOnQueryTextListener(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        gson = new Gson();

        new SortNoteTask().execute();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        searchView.setQuery("", false);
        searchView.setIconified(true);
        dataBaseHandler.setOffset();
        new SortNoteTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.from_new_to_old:
                return startFilter(0);
            case R.id.from_old_to_new:
                return startFilter(1);
        }
        return false;
    }

    private boolean startFilter(int count) {
        this.count = count;
        searchView.setIconified(true);
        dataBaseHandler.setOffset();
        dataBaseHandler.setOffset();
        new SortNoteTask().execute();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {

        if (adapter.getmFilteredList() != null) {
            List<Note> notesList = new ArrayList<>();
            for (Note note : adapter.getmFilteredList()) {
                if (note.getContent().toLowerCase().contains(s.toLowerCase())) {
                    notesList.add(note);
                }
            }
            adapter.updateList(notesList);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.add_button) {
            Intent intent = new Intent(this, AddEditActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRowClick(int position, Note note) {
        Cursor data = dataBaseHandler.getItemId(note.getTimeInMillis());
        int itemId = -1;
        while (data.moveToNext()) {
            itemId = data.getInt(0);
        }

        if (itemId > -1) {
            Intent intent = new Intent(this, AddEditActivity.class);
            intent.putExtra("ID", itemId);
            intent.putExtra("TIME", note.getTimeInMillis());
            intent.putExtra("NOTE", gson.toJson(note));
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void addMore() {
        dataBaseHandler.plusOffSet();
        new GetMoreNoteTask().execute();
    }

    @Override
    public void onRefresh() {
        adapter.clearList();
        dataBaseHandler.setOffset();
        new SortNoteTask().execute();
    }

    //ItemTouchHelper

    class MyTouchCallBack extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.END |
                    ItemTouchHelper.START);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            final int position = viewHolder.getAdapterPosition();
            final Note removed = adapter.remove(position);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Удалить?")
                    .setMessage("Вы хотите удалить эту заметку?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Cursor cursor = dataBaseHandler.getItemId(removed.getTimeInMillis());
                            int itemId = -1;
                            while (cursor.moveToNext()) {
                                itemId = cursor.getInt(0);
                            }
                            if (itemId > -1) {
                                dataBaseHandler.deleteNote(itemId, removed.getContent());
                            }
                        }
                    }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.cancelLastRemove();
                }
            }).create()
                    .show();
        }
    }

    //AsyncTask

    class SortNoteTask extends AsyncTask<Void, Void, List<Note>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Note> doInBackground(Void... voids) {
            list = new ArrayList<>();
            Cursor data = dataBaseHandler.sortFromNewToOld();
            if (count == 1) {
                data = dataBaseHandler.sortFromOldToNew();
            }
            while (data.moveToNext()) {
                String content = data.getString(1);
                String date = data.getString(2);
                long timeInMillis = data.getLong(3);
                Note note = new Note(content, date, timeInMillis);
                list.add(note);
            }

            adapter = new NotesListAdapter(list, MainActivity.this, MainActivity.this);
            return list;
        }

        @Override
        protected void onPostExecute(List<Note> list) {
            if (list != null) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                }
                LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                notesList.setLayoutManager(layoutManager);
                ItemTouchHelper helper = new ItemTouchHelper(new MyTouchCallBack());
                helper.attachToRecyclerView(notesList);
                notesList.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    class GetMoreNoteTask extends AsyncTask<Void, Void, List<Note>> {

        @Override
        protected List<Note> doInBackground(Void... voids) {
            list = new ArrayList<>();
            Cursor data = dataBaseHandler.sortFromNewToOld();
            if (count == 1) {
                data = dataBaseHandler.sortFromOldToNew();
            }
            while (data.moveToNext()) {
                String content = data.getString(1);
                String date = data.getString(2);
                long timeInMillis = data.getLong(3);
                Note note = new Note(content, date, timeInMillis);
                list.add(note);
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Note> list) {
            if (list != null) {
                adapter.addAll(list);
                if (list.size() < 20) {
                    adapter.hideFooter();
                }
            }
        }
    }
}
