package algonquin.cst2335.wang0467;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.wang0467.databinding.ActivityChatRoomBinding;
import algonquin.cst2335.wang0467.databinding.ReceiveMessageBinding;
import algonquin.cst2335.wang0467.databinding.SentMessageBinding;

public class ChatRoom extends AppCompatActivity {
    ActivityChatRoomBinding binding;
    ChatRoomViewModel chatModel;
    private RecyclerView.Adapter myAdapter;
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
                if(viewType == 0) {
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
                return chatMessage.isSendButton() ? 0: 1;
            }
        });

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(), MessageDatabase.class, "database-name").build();
        mDAO = db.cmDAO();

        ArrayList<ChatMessage> currentMessages = chatModel.messages.getValue();
        if(currentMessages == null) {
            ArrayList<ChatMessage> newMessagesList = new ArrayList<>();
            chatModel.messages.setValue(newMessagesList);

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                newMessagesList.addAll(mDAO.getAllMessages()); // Once you get the data from the database

                runOnUiThread(() -> {
                    binding.recycleView.setAdapter(myAdapter); // You can then load the RecyclerView
                });
            });
        }

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
}

class MyRowHolder extends RecyclerView.ViewHolder {
    TextView messageText;
    TextView timeText;
    public MyRowHolder(@NonNull View itemView) {
        super(itemView);

        messageText = itemView.findViewById(R.id.messageText);
        timeText = itemView.findViewById(R.id.timeText);
    }
}
