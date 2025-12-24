package com.myapp.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private List<Library> libraries;
    private Context context;
    private OnLibraryLongClickListener longClickListener;

    public interface OnLibraryLongClickListener {
        void onLibraryLongClick(Library library, int position);
    }

    public LibraryAdapter(Context context, List<Library> libs) {
        this.context = context;
        this.libraries = libs;
    }

    public void setOnLibraryLongClickListener(OnLibraryLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_library, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Library lib = libraries.get(position);
        holder.tvName.setText(lib.getName());
        holder.tvDesc.setText(lib.getDescription() != null && !lib.getDescription().isEmpty() ? lib.getDescription() : "Không có mô tả");
        holder.tvTags.setText(lib.getTags() != null ? lib.getTags() : "");

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onLibraryLongClick(lib, position);
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            LibraryContentActivity.open(context, lib);
        });
    }

    @Override
    public int getItemCount() { return libraries.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvTags;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLibName);
            tvDesc = itemView.findViewById(R.id.tvLibDescription);
            tvTags = itemView.findViewById(R.id.tvLibTags);
        }
    }
}
