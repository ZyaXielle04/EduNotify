package com.zyacodes.edunotifyproj.Adapters;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.zyacodes.edunotifyproj.Models.Post;
import com.zyacodes.edunotifyproj.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        int adapterPos = holder.getAdapterPosition();
        if (adapterPos == RecyclerView.NO_POSITION) return;

        Post post = postList.get(adapterPos);

        // Set post content
        holder.content.setText(post.getContent());

        // Likes & comments count
        int likeCount = post.getLikedBy() != null ? post.getLikedBy().size() : 0;
        int commentCount = post.getComments() != null ? post.getComments().size() : 0;
        holder.likes.setText(likeCount + " Likes");
        holder.commentCount.setText(commentCount + " Comments");

        // Timestamp
        holder.timestamp.setText(getRelativeTime(post.getTimestamp()));

        // Load post author
        DatabaseReference postAuthorRef = FirebaseDatabase.getInstance(
                        "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(post.getPostedBy()).child("username");
        postAuthorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.exists() ? snapshot.getValue(String.class) : "Anonymous";
                holder.author.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.author.setText("Anonymous");
            }
        });

        // Like button
        holder.btnLike.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Post p = postList.get(pos);
            String postId = p.getPostId();
            if (postId == null) return;

            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            if (uid == null) return;

            DatabaseReference likeRef = FirebaseDatabase.getInstance(
                            "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("posts").child(postId).child("likedBy");

            likeRef.child(uid).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        if (p.getLikedBy() == null) p.setLikedBy(new HashMap<>());
                        p.getLikedBy().put(uid, true);
                        notifyItemChanged(pos);
                        Toast.makeText(holder.itemView.getContext(), "Liked!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(),
                            "Failed to like: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Post new comment
        holder.btnPostComment.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Post p = postList.get(pos);

            String postId = p.getPostId();
            if (postId == null) return;

            String commentText = holder.etNewComment.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) return;

            DatabaseReference commentsRef = FirebaseDatabase.getInstance(
                            "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("posts").child(postId).child("comments");

            String commentId = commentsRef.push().getKey();
            if (commentId == null) return;

            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

            Post.Comment newComment = new Post.Comment(commentText, uid, System.currentTimeMillis());

            commentsRef.child(commentId).setValue(newComment)
                    .addOnSuccessListener(aVoid -> {
                        if (p.getComments() == null) p.setComments(new HashMap<>());
                        p.getComments().put(commentId, newComment);
                        notifyItemChanged(pos);
                        holder.etNewComment.setText("");
                        Toast.makeText(holder.itemView.getContext(), "Comment added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(),
                            "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Display comments
        holder.commentContainer.removeAllViews();
        holder.tvViewAllComments.setVisibility(View.GONE);

        if (post.getComments() != null) {
            List<Post.Comment> comments = new ArrayList<>(post.getComments().values());
            comments.sort((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp())); // newest first

            // Show first 3 comments by default
            int initialDisplay = Math.min(3, comments.size());
            for (int i = 0; i < initialDisplay; i++) {
                addCommentView(holder, comments.get(i));
            }

            // Show "View all comments" if more than 2
            if (comments.size() > 3) {
                holder.tvViewAllComments.setVisibility(View.VISIBLE);
                holder.tvViewAllComments.setText("View all comments (" + comments.size() + ")");
                holder.tvViewAllComments.setOnClickListener(v -> {
                    holder.commentContainer.removeAllViews();
                    for (Post.Comment c : comments) {
                        addCommentView(holder, c);
                    }
                    holder.tvViewAllComments.setVisibility(View.GONE);
                });
            }
        }
    }

    private String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 0) diff = 0; // future-proof

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) return "Just now";
        else if (minutes < 60) return minutes + " min" + (minutes > 1 ? "s" : "") + " ago";
        else if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        else if (days == 1) return "Yesterday";
        else return days + " days ago";
    }

    private void addCommentView(PostViewHolder holder, Post.Comment comment) {
        LinearLayout commentLayout = new LinearLayout(holder.itemView.getContext());
        commentLayout.setOrientation(LinearLayout.VERTICAL);
        commentLayout.setPadding(12, 8, 12, 8);
        commentLayout.setBackgroundColor(0xFFEFEFEF);

        TextView authorTv = new TextView(holder.itemView.getContext());
        authorTv.setTextSize(16f);
        authorTv.setTypeface(null, android.graphics.Typeface.BOLD);
        // Load username
        DatabaseReference commentUserRef = FirebaseDatabase.getInstance(
                        "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(comment.getPostedBy()).child("username");
        commentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.exists() ? snapshot.getValue(String.class) : "Anonymous";
                authorTv.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                authorTv.setText("Anonymous");
            }
        });
        commentLayout.addView(authorTv);

        TextView contentTv = new TextView(holder.itemView.getContext());
        contentTv.setText(comment.getContent());
        contentTv.setTextSize(14f);
        commentLayout.addView(contentTv);

        TextView timestampTv = new TextView(holder.itemView.getContext());
        timestampTv.setText(new SimpleDateFormat("dd MMM yyyy | HH:mm", Locale.getDefault())
                .format(new Date(comment.getTimestamp())));
        timestampTv.setTextSize(12f);
        timestampTv.setTextColor(Color.GRAY);
        commentLayout.addView(timestampTv);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 4, 0, 4);
        holder.commentContainer.addView(commentLayout, layoutParams);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView content, author, timestamp, likes, commentCount, tvViewAllComments;
        Button btnLike, btnComment;
        EditText etNewComment;
        ImageButton btnPostComment;
        LinearLayout commentContainer;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.tvContent);
            author = itemView.findViewById(R.id.tvAuthor);
            timestamp = itemView.findViewById(R.id.tvTimestamp);
            likes = itemView.findViewById(R.id.tvLikes);
            commentCount = itemView.findViewById(R.id.tvCommentCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            etNewComment = itemView.findViewById(R.id.etNewComment);
            btnPostComment = itemView.findViewById(R.id.btnPostComment);
            commentContainer = itemView.findViewById(R.id.commentContainer);
            tvViewAllComments = itemView.findViewById(R.id.tvViewAllComments);
        }
    }
}
