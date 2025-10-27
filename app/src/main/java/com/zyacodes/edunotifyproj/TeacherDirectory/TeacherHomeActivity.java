package com.zyacodes.edunotifyproj.TeacherDirectory;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

public class TeacherHomeActivity extends AppCompatActivity {

    private TextView inputPost;
    private LinearLayout navDashboard, navHome, navUsers, navEvents, navSettings;
    private NestedScrollView mainScrollView;
    private FirebaseAuth mAuth;
    private Dialog currentDialog;

    private RecyclerView recyclerPosts;
    private List<Post> postList;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        mAuth = FirebaseAuth.getInstance();

        // Input trigger
        inputPost = findViewById(R.id.inputPost);
        inputPost.setOnClickListener(v -> showCreatePostDialog());

        // Navbar
        navDashboard = findViewById(R.id.navDashboard); // Classes
        navHome = findViewById(R.id.navHome);
        navUsers = findViewById(R.id.navUsers); // Logout
        navEvents = findViewById(R.id.navEvents);
        navSettings = findViewById(R.id.navSettings); // Optional: Settings
        mainScrollView = findViewById(R.id.mainScrollView);

        setupNavbar();

        // RecyclerView setup
        recyclerPosts = findViewById(R.id.recyclerPosts);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(postAdapter);

        loadPostsFromFirebase();
    }

    private void setupNavbar() {
        // Scroll to top
        navHome.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of your News Feed", Toast.LENGTH_SHORT).show();
            }
        });

        // Open Classes
        navDashboard.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Dashboard...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TeacherDashboardActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Events
        navEvents.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Events / Reports...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TeacherEventsActivity.class)); // create this if needed
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Settings (optional)
        navSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TeacherSettingsActivity.class)); // create if needed
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    // -------------------------------------------------------------------
    // CREATE POST DIALOG (TEXT ONLY)
    // -------------------------------------------------------------------
    private void showCreatePostDialog() {
        currentDialog = new Dialog(this);
        currentDialog.setContentView(R.layout.dialog_create_post);
        if (currentDialog.getWindow() != null) {
            currentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText postContent = currentDialog.findViewById(R.id.editTextPostContent);
        Button btnPost = currentDialog.findViewById(R.id.btnPost);
        Button btnCancel = currentDialog.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> currentDialog.dismiss());

        btnPost.setOnClickListener(v -> {
            String content = postContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Post cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            savePostToFirebase(content);
        });

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        if (currentDialog.getWindow() != null)
            currentDialog.getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        currentDialog.show();
    }

    // -------------------------------------------------------------------
    // SAVE TEXT POST TO FIREBASE
    // -------------------------------------------------------------------
    private void savePostToFirebase(String content) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        DatabaseReference postsRef = FirebaseDatabase.getInstance(
                        "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("posts");

        String postId = postsRef.push().getKey();
        if (postId == null) return;

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("content", content);
        postMap.put("postedBy", userId);
        postMap.put("timestamp", System.currentTimeMillis());
        postMap.put("likedBy", new HashMap<>()); // initially empty
        postMap.put("comments", new HashMap<>()); // initially empty

        postsRef.child(postId).setValue(postMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show();
                    if (currentDialog != null && currentDialog.isShowing()) currentDialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // -------------------------------------------------------------------
    // LOAD POSTS FROM FIREBASE
    // -------------------------------------------------------------------
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
                Toast.makeText(TeacherHomeActivity.this, "Failed to load posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
