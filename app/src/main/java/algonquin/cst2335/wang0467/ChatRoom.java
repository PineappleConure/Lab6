package algonquin.cst2335.wang0467;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.wang0467.databinding.ActivityChatRoomBinding;
import algonquin.cst2335.wang0467.databinding.ReceiveMessageBinding;
import algonquin.cst2335.wang0467.databinding.SentMessageBinding;

public class ChatRoom extends AppCompatActivity {
    ActivityChatRoomBinding binding;
    ChatRoomViewModel chatModel;
    private RecyclerView.Adapter<MyRowHolder> myAdapter;
    private ChatMessageDAO mDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        if (chatModel.messages.getValue() == null) {
            chatModel.messages.postValue(new ArrayList<>());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");

        binding.sendButton.setOnClickListener(click -> {
            String message = binding.messageEditText.getText().toString();
            String currentDateandTime = sdf.format(new Date());
            ChatMessage newMessage = new ChatMessage(message, currentDateandTime, true);
            addMessageToModel(newMessage);
        });

        binding.receiveButton.setOnClickListener(click -> {
            String message = binding.messageEditText.getText().toString();
            String currentDateandTime = sdf.format(new Date());
            ChatMessage newMessage = new ChatMessage(message, currentDateandTime, false);
            addMessageToModel(newMessage);
        });

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
        binding.recycleView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == 0) {
                    SentMessageBinding binding = SentMessageBinding.inflate(getLayoutInflater());
                    return new MyRowHolder(binding.getRoot());
                } else {
                    ReceiveMessageBinding binding = ReceiveMessageBinding.inflate(getLayoutInflater());
                    return new MyRowHolder(binding.getRoot());
                }
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                ChatMessage chatMessage = chatModel.messages.getValue().get(position);
                holder.messageText.setText(chatMessage.getMessage());
                holder.timeText.setText(chatMessage.getTimeSent());
            }

            @Override
            public int getItemCount() {
                return chatModel.messages.getValue().size();
            }

            @Override
            public int getItemViewType(int position) {
                ChatMessage chatMessage = chatModel.messages.getValue().get(position);
                return chatMessage.isSendButton() ? 0 : 1;
            }
        });

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(),
                MessageDatabase.class, "database-name").build();
        mDAO = db.cmDAO();

        // Load messages from the database initially
        loadMessagesFromDatabase();
    }

    private void addMessageToModel(ChatMessage newMessage) {
        ArrayList<ChatMessage> currentMessages = chatModel.messages.getValue();
        if (currentMessages != null) {
            currentMessages.add(newMessage);
            chatModel.messages.postValue(currentMessages);
            myAdapter.notifyItemInserted(currentMessages.size() - 1);
            binding.messageEditText.setText("");
        }
    }

    private void loadMessagesFromDatabase() {
        Executor thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            ArrayList<ChatMessage> newMessagesList = new ArrayList<>(mDAO.getAllMessages());
            runOnUiThread(() -> {
                chatModel.messages.setValue(newMessagesList);
                myAdapter.notifyDataSetChanged();
            });
        });
    }

    class MyRowHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public MyRowHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);

            itemView.setOnClickListener(clk -> {
                int position = getAbsoluteAdapterPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoom.this);
                builder.setMessage("Do you want to delete this message?")
                        .setTitle("Confirm")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", (dialog, cl) -> deleteMessage(position))
                        .show();
            });
        }

        private void deleteMessage(int position) {
            ArrayList<ChatMessage> currentMessages = chatModel.messages.getValue();
            ChatMessage messageToDelete = currentMessages.get(position);

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                mDAO.deleteMessage(messageToDelete);
                runOnUiThread(() -> {
                    currentMessages.remove(position);
                    myAdapter.notifyItemRemoved(position);
                    showUndoSnackbar(messageToDelete, position);
                });
            });
        }

        private void showUndoSnackbar(ChatMessage message, int position) {
            Snackbar.make(messageText, "Message deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> undoDelete(message, position))
                    .show();
        }

        private void undoDelete(ChatMessage message, int position) {
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                mDAO.insertMessage(message);
                runOnUiThread(() -> {
                    ArrayList<ChatMessage> currentMessages = chatModel.messages.getValue();
                    currentMessages.add(position, message);
                    myAdapter.notifyItemInserted(position);
                });
            });
        }
    }

}
