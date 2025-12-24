package com.myapp.project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.io.File;
import java.util.List;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document doc = documents.get(position);

        holder.tvName.setText(doc.getName());
        holder.tvDescription.setText(doc.getDescription() != null ? doc.getDescription() : "Không có mô tả");

        if (doc.getTags() != null && !doc.getTags().isEmpty()) {
            holder.tvTags.setVisibility(View.VISIBLE);
            holder.tvTags.setText(doc.getTags());
        } else {
            holder.tvTags.setVisibility(View.GONE);
        }

        String type = doc.getFileType();

        // --- XỬ LÝ HIỂN THỊ: ẢNH THẬT hoặc ICON MÀU GALAXY ---
        if ("IMAGE".equals(type)) {
            // 1. Nếu là Ảnh: Xóa nền màu, dùng Glide load ảnh thật
            holder.layoutIcon.setBackground(null);
            holder.ivFileType.clearColorFilter(); // Xóa lớp phủ màu trắng

            Glide.with(context)
                    .load(new File(doc.getFilePath()))
                    .transform(new CircleCrop())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.ivFileType);

        } else {
            // 2. Nếu là PDF/Text: Dùng nền màu theo Theme
            Glide.with(context).clear(holder.ivFileType); // Xóa ảnh cũ
            holder.layoutIcon.setBackgroundResource(R.drawable.bg_circle_icon);

            // Màu mặc định (Xám)
            int color = Color.parseColor("#757575");
            int iconRes = android.R.drawable.ic_menu_info_details;

            if ("PDF".equals(type)) {
                color = Color.parseColor("#7B1FA2"); // Tím (Purple) - Hợp với Gradient cuối
                iconRes = android.R.drawable.ic_menu_sort_by_size;
            } else if ("TXT".equals(type)) {
                color = Color.parseColor("#2196F3"); // Xanh (Blue) - Hợp với Gradient đầu
                iconRes = android.R.drawable.ic_menu_edit;
            }

            // Đổi màu nền tròn
            if (holder.layoutIcon.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) holder.layoutIcon.getBackground()).setColor(color);
            }

            holder.ivFileType.setImageResource(iconRes);
            holder.ivFileType.setColorFilter(Color.WHITE); // Icon trắng nổi bật
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DocumentDetailActivity.class);
            intent.putExtra("document", doc);
            context.startActivity(intent);
        });

        holder.btnMore.setOnClickListener(v -> {
            if (longClickListener != null) longClickListener.onDocumentLongClick(doc, position);
        });
    }

    @Override
    public int getItemCount() { return documents.size(); }
    public void updateList(List<Document> docs) { this.documents = docs; notifyDataSetChanged(); }
    public void removeItem(int position) { documents.remove(position); notifyItemRemoved(position); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        TextView tvName, tvDescription, tvTags;
        ImageView ivFileType, btnMore;
        View layoutIcon;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardDocument);
            layoutIcon = itemView.findViewById(R.id.layoutIcon);
            ivFileType = itemView.findViewById(R.id.ivFileType);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTags = itemView.findViewById(R.id.tvTags);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
