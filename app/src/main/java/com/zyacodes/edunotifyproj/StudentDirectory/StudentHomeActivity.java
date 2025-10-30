package com.zyacodes.edunotifyproj.StudentDirectory; // or ParentDirectory

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.zyacodes.edunotifyproj.Adapters.PostAdapter;
import com.zyacodes.edunotifyproj.Models.Post;
import com.zyacodes.edunotifyproj.R;

import java.util.*;

public class StudentHomeActivity extends AppCompatActivity {

    private LinearLayout navDashboard, navHome, navEvents, navSettings;
    private NestedScrollView mainScrollView;

    private RecyclerView recyclerPosts;
    private List<Post> postList;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home); // separate XML without inputPost

        // Navbar
        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navEvents = findViewById(R.id.navEvents);
        navSettings = findViewById(R.id.navSettings);
        mainScrollView = findViewById(R.id.mainScrollView);

        setupNavbar();

        // RecyclerView setup
        recyclerPosts = findViewById(R.id.recyclerPosts);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList); // adapter already handles likes/comments
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(postAdapter);

        loadPostsFromFirebase();
    }

    private void setupNavbar() {
        navHome.setOnClickListener(v -> {
            if (mainScrollView != null) mainScrollView.smoothScrollTo(0, 0);
        });

        navDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentDashboardActivity.class));
        });

        navEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentEventsActivity.class));
        });

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentSettingsActivity.class));
        });
    }

    private void loadPostsFromFirebase() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance(
                        "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("posts");

        postsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null) postList.add(0, post); // newest first
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentHomeActivity.this,
                        "Failed to load posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
