package edu.gvsu.cis.jobquals;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
//    private Button clearSearchesBtn;
    private Button cancelBtn;
    private FirebaseDatabase mDatabase;
//    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
//    private FirebaseUser user;
//    private String userID;
    private ArrayList<String> recentSearches;
    private String selected = null;
    int max = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searches);

        selectBtn = findViewById(R.id.selectBtn);
//        clearSearchesBtn = findViewById(R.id.clearSearchesBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
//        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
//        user = mAuth.getCurrentUser();
//        userID = user.getUid();
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
//        clearSearchesBtn.setOnClickListener(v -> clearSearches());
        cancelBtn.setOnClickListener(v -> cancel());
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchRecyclerAdapter(this, recentSearches);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

//    private void clearSearches() {
//        mRef.child("users").removeValue();
//        recentSearches.clear();
//        setUpRecyclerView();
//    }

    private void cancel() {
        Intent i = new Intent(SearchRecyclerActivity.this, MainActivity.class);
        setResult(Activity.RESULT_CANCELED, i);
        finish();
    }

    private void select() {
        Intent i = new Intent(SearchRecyclerActivity.this, MainActivity.class);
//        if (selected != null)
        i.putExtra("Selected", selected.toString());
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        selected = adapter.getItem(position);
    }
}
