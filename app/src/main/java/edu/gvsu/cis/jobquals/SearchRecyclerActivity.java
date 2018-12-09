package edu.gvsu.cis.jobquals;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SearchRecyclerActivity extends AppCompatActivity implements SearchRecyclerAdapter.ItemClickListener {

    SearchRecyclerAdapter adapter;
    private Button selectBtn;
    private Button cancelBtn;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private ArrayList<String> recentSearches;
    private String selected = null;
    int max = 25;

    private int primaryColor;
    private int secondaryColor;
    private int backgroundColor;
    private int editTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searches);

        selectBtn = findViewById(R.id.selectBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        recentSearches = new ArrayList<>();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recentSearches.clear();
                int currentVal = 0;
                Iterable<DataSnapshot> iter = dataSnapshot.child("users").getChildren();
                List<DataSnapshot> list = new ArrayList<DataSnapshot>();
                for (DataSnapshot item : iter) {
                    list.add(item);
                }
                Collections.reverse(list);
                for (DataSnapshot childSnapshot : list) {
                    if (++currentVal <= max) {
                        String temp = childSnapshot.getValue(String.class);
                        recentSearches.add(temp);
                        setUpRecyclerView();
                    } else {
                        childSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        selectBtn.setOnClickListener(v -> select());
        cancelBtn.setOnClickListener(v -> cancel());

        Intent intent = getIntent();
        boolean dark = intent.getBooleanExtra("Dark", false);

        if (dark == true) {
            primaryColor = getResources().getColor(R.color.dark_text);
            secondaryColor = getResources().getColor(R.color.dark_actionbar);
            backgroundColor = getResources().getColor(R.color.dark_background);
            editTextColor = getResources().getColor(R.color.dark_text_border);
        } else {
            primaryColor = getResources().getColor(R.color.minimal_dusty);
            secondaryColor = getResources().getColor(R.color.minimal_lavender);
            backgroundColor = getResources().getColor(R.color.minimal_overcast);
            editTextColor = getResources().getColor(R.color.minimal_paper);
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(secondaryColor));
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setBackgroundColor(backgroundColor);
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchRecyclerAdapter(this, recentSearches);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setBackgroundColor(editTextColor);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void cancel() {
        Intent i = new Intent(SearchRecyclerActivity.this, MainActivity.class);
        setResult(Activity.RESULT_CANCELED, i);
        finish();
    }

    private void select() {
        Intent i = new Intent(SearchRecyclerActivity.this, MainActivity.class);
        i.putExtra("Selected", selected.toString());
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
        selected = adapter.getItem(position);
    }
}
