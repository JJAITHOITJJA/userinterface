package com.example.myapplication.presentation.group;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import com.example.myapplication.R;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.OnItemClickListener;
import com.example.myapplication.data.group.DiscussionItem;

import org.jspecify.annotations.NonNull;

public class DiscussionAdapter extends ListAdapter<DiscussionItem, DiscussionAdapter.DiscussionViewHolder> {

    private OnItemClickListener<DiscussionItem> listener = null;

    public DiscussionAdapter(){
        super(new DiscussionAdapter.DiscussionItemDiffCallback());
    }
    public DiscussionAdapter(OnItemClickListener<DiscussionItem> listener){
        super(new DiscussionAdapter.DiscussionItemDiffCallback());
        this.listener= listener;
    }

    @NonNull
    @Override
    public DiscussionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_book_list, parent, false);
        return new DiscussionAdapter.DiscussionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DiscussionViewHolder holder, int position){
        DiscussionItem item = getItem(position);
        holder.bind(item);

        holder.itemView.setOnClickListener(v->{
            if(listener!= null&& holder.getAdapterPosition() != RecyclerView.NO_POSITION){
                listener.onItemClick(getItem(holder.getAdapterPosition()), holder.getAdapterPosition());
            }
        });
    }

    private static class DiscussionItemDiffCallback extends DiffUtil.ItemCallback<DiscussionItem>{
        @Override
        public boolean areItemsTheSame(@NonNull DiscussionItem oldItem, @NonNull DiscussionItem newItem){
            return oldItem.getDiscussionId().equals(newItem.getDiscussionId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(DiscussionItem oldItem, DiscussionItem newItem){
            return oldItem.equals(newItem);
        }
    }
    public static class DiscussionViewHolder extends RecyclerView.ViewHolder{

        private final TextView discussionBookname;
        private final TextView topic;
        private final TextView author;
        public DiscussionViewHolder(View itemView){
            super(itemView);
            discussionBookname = itemView.findViewById(R.id.tv_discussion_book_name);
            topic = itemView.findViewById(R.id.tv_discuss_topic);
            author = itemView.findViewById(R.id.tv_author_name);

        }

        public void bind(DiscussionItem item){
            discussionBookname.setText(item.getBookName());
            topic.setText(item.getTopic());
            author.setText(item.getAuthor());
        }
    }
}
