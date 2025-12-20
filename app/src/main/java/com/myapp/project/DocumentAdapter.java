package com.myapp.project;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private List<Document> documents;
    private Context context;
    private OnDocumentLongClickListener longClickListener;

    public interface OnDocumentLongClickListener {
        void onDocumentLongClick(Document document, int position);
    }

    public DocumentAdapter(Context context, List<Document> documents) {
        this.context = context;
        this.documents = documents;
    }

    public void setOnDocumentLongClickListener(OnDocumentLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document doc = documents.get(position);

        holder.tvName.setText(doc.getName());
        holder.tvDescription.setText(
                doc.getDescription() != null && !doc.getDescription().isEmpty()
                        ? doc.getDescription()
                        : "Không có mô tả"
        );

        switch (doc.getFileType()) {
            case "PDF":
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_recent_history);
                break;
            case "TXT":
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_edit);
                break;
            case "IMAGE":
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_gallery);
                break;
            default:
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(doc.getLastModified())));

        holder.tvTags.setVisibility(
                doc.getTags() != null && !doc.getTags().isEmpty() ? View.VISIBLE : View.GONE
        );
        holder.tvTags.setText(doc.getTags());

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DocumentDetailActivity.class);
            intent.putExtra("document", doc);
            context.startActivity(intent);
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onDocumentLongClick(doc, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public void updateList(List<Document> docs) {
        this.documents = docs;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        documents.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        TextView tvName, tvDescription, tvDate, tvTags;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTags = itemView.findViewById(R.id.tvTags);
        }
    }
}
