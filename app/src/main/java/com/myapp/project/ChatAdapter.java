package com.myapp.project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import io.noties.markwon.Markwon; // Thư viện làm đẹp chữ

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private List<ChatMessage> chatMessages;
    private Markwon markwon; // Biến xử lý hiển thị chữ đẹp
    private OnMessageAction actionListener;

    // Interface giao tiếp
    public interface OnMessageAction {
        void onEdit(String text);
    }

    // Constructor chuẩn
    public ChatAdapter(Context context, List<ChatMessage> chatMessages, OnMessageAction listener) {
        this.chatMessages = chatMessages;
        // KHỞI TẠO MARKWON (QUAN TRỌNG ĐỂ HIỆN CHỮ ĐẬM/NGHIÊNG)
        this.markwon = Markwon.create(context);
        this.actionListener = listener;
    }

    // Constructor phụ (để tránh lỗi nếu code cũ gọi thiếu tham số)
    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this(context, chatMessages, null);
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        TextView tvMessage;

        if (holder instanceof UserViewHolder) {
            tvMessage = ((UserViewHolder) holder).tvMessage;
            // Tin nhắn User: Hiển thị text thường
            tvMessage.setText(message.getText());
        } else {
            tvMessage = ((AIViewHolder) holder).tvMessage;
            // Tin nhắn AI: Dùng MARKWON để render (SỬA LỖI Ở ĐÂY)
            // Nó sẽ tự động biến **text** thành chữ in đậm
            markwon.setMarkdown(tvMessage, message.getText());
        }

        // Sự kiện nhấn giữ (Copy/Edit)
        tvMessage.setOnLongClickListener(v -> {
            showOptionsDialog(v.getContext(), message);
            return true;
        });
    }

    private void showOptionsDialog(Context context, ChatMessage message) {
        CharSequence[] options;
        if (message.isUser()) {
            options = new CharSequence[]{"Sao chép", "Chỉnh sửa câu hỏi"};
        } else {
            options = new CharSequence[]{"Sao chép phản hồi"};
        }

        new AlertDialog.Builder(context)
                .setTitle("Tùy chọn")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Sao chép
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Chat Content", message.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "Đã sao chép!", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) { // Chỉnh sửa
                        if (actionListener != null) {
                            actionListener.onEdit(message.getText());
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        UserViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        AIViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}