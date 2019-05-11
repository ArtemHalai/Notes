package com.example.notes.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.notes.R;
import com.example.notes.data.DataBaseHandler;
import com.example.notes.data.StoreProvider;
import com.example.notes.data.dto.Note;
import com.google.gson.Gson;


public class AddEditActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView backBtn;
    private Toolbar toolbar;
    private EditText addEditTxt;
    private DataBaseHandler dataBaseHandler;
    private Gson gson;
    private int selectedId;
    private String selectedContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        toolbar = findViewById(R.id.toolbar);
        backBtn = findViewById(R.id.back_btn);
        addEditTxt = findViewById(R.id.add_edit_txt);

        backBtn.setOnClickListener(this);

        gson = new Gson();

        Intent intent = getIntent();
        selectedId = intent.getIntExtra("ID", -1);
        if (selectedId > 0) {
            String str = intent.getStringExtra("NOTE");
            Note note = gson.fromJson(str, Note.class);
            addEditTxt.setText(note.getContent());
            selectedContent = note.getContent();
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        dataBaseHandler = new DataBaseHandler(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back_btn) {
            showDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.remove_btn:
                removeNote();
                return true;
            case R.id.share_btn:
                if (addEditTxt.getText().toString().trim().isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setMessage("Поле заметки пустое, нечем поделиться!")
                            .setPositiveButton("Ok", null)
                            .setCancelable(false)
                            .create()
                            .show();
                } else {
                    shareNote();
                }
                return true;
        }
        return false;
    }

    private void shareNote() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        if (selectedId > 0) {
            share.putExtra(Intent.EXTRA_SUBJECT, "Note: " + selectedId);
        }
        share.putExtra(Intent.EXTRA_TEXT, addEditTxt.getText().toString());
        startActivity(Intent.createChooser(share, "Поделиться"));
    }

    private void removeNote() {
        if (selectedId > 0) {
            dataBaseHandler.deleteNote(selectedId, selectedContent);
            setResult(RESULT_OK);
            finish();
        } else {
            addEditTxt.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        showDialog();
    }

    public void showDialog() {
        if (addEditTxt.getText().toString().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage("Поле заметки пустое." + "\n" + "Вы уверены, что хотите покинуть эту страницу?")
                    .setCancelable(false)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            if (selectedId > 0) {
                long timeInMillis = System.currentTimeMillis();
                String date = StoreProvider.getInstance().getDate(timeInMillis);
                dataBaseHandler.updateNote(addEditTxt.getText().toString(), selectedId, selectedContent,
                        timeInMillis, date);
                setResult(RESULT_OK);
                finish();
            } else {
                long time = System.currentTimeMillis();
                String date = StoreProvider.getInstance().getDate(time);
                String content = addEditTxt.getText().toString();
                Note note = new Note(content, date, time);
                dataBaseHandler.addNote(note);
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
