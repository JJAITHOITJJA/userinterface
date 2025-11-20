package com.example.myapplication.presentation.group.discussion;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.data.group.CommentItem;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import com.example.myapplication.R;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_COMMENT = 0;
    private static final int TYPE_REPLY = 1;
    private OnCommentActionListener listener;
    private List<CommentItem> items = new ArrayList<>();

    public interface OnCommentActionListener {
        void onReplyClick(CommentItem item, int position);
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isReply() ? TYPE_REPLY : TYPE_COMMENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_COMMENT) {
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_recomment, parent, false);
            return new ReplyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentItem item = items.get(position);

        if (holder instanceof CommentViewHolder) {

            ((CommentViewHolder) holder).bind(item, position, listener);
        } else if (holder instanceof ReplyViewHolder) {
            ((ReplyViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<CommentItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView nickname;
        TextView content;
        TextView page;
        TextView createdAt;
        ImageView profileImageUrl;
        TextView reply;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.item_comment_username);
            content = itemView.findViewById(R.id.tv_comment_content);
            page = itemView.findViewById(R.id.item_comment_page);
            createdAt = itemView.findViewById(R.id.tv_comment_date);
            profileImageUrl = itemView.findViewById(R.id.comment_owner_profile_img);
            reply = itemView.findViewById(R.id.btn_recomment);
        }
        public void bind(CommentItem item, int position, OnCommentActionListener listener) {
            nickname.setText(item.getNickname());
            content.setText(item.getContent());
            page.setText("p." + item.getPage());

            if (item.getCreatedAt() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
                createdAt.setText(dateFormat.format(item.getCreatedAt()));
            } else {
                createdAt.setText("");
            }

            if (item.getProfileImageUrl() != null && !item.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getProfileImageUrl())
                        .placeholder(R.drawable.capibara)
                        .error(R.drawable.capibara)
                        .into(profileImageUrl);
            } else {
                profileImageUrl.setImageResource(R.drawable.capibara);
            }

            reply.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onReplyClick(item, pos);
                    }
                }
            });
        }
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView nickname;
        TextView content;
        TextView createdAt;
        ImageView profileImageUrl;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.item_recomment_username);
            content = itemView.findViewById(R.id.tv_recomment_content);
            createdAt = itemView.findViewById(R.id.tv_recomment_date);
            profileImageUrl = itemView.findViewById(R.id.recomment_owner_profile_img);
        }

        public void bind(CommentItem item) {
            nickname.setText(item.getNickname());
            content.setText(item.getContent());

            if (item.getCreatedAt() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
                createdAt.setText(dateFormat.format(item.getCreatedAt()));
            } else {
                createdAt.setText("");
            }

            if (item.getProfileImageUrl() != null && !item.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getProfileImageUrl())
                        .placeholder(R.drawable.capibara)
                        .error(R.drawable.capibara)
                        .into(profileImageUrl);
            } else {
                profileImageUrl.setImageResource(R.drawable.capibara);
            }
        }
    }
}
