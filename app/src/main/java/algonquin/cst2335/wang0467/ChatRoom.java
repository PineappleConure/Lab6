package algonquin.cst2335.wang0467;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import algonquin.cst2335.wang0467.databinding.ActivityChatRoomBinding;
import algonquin.cst2335.wang0467.databinding.SentMessageBinding;

public class ChatRoom extends AppCompatActivity {
    ActivityChatRoomBinding binding;
    ChatRoomViewModel chatModel;
    private RecyclerView.Adapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);

        if (chatModel.messages.getValue() == null) {
            chatModel.messages.postValue(new ArrayList<>());
        }

        binding.sendButton.setOnClickListener(click -> {
            ArrayList<String> currentMessages = chatModel.messages.getValue();
            if (currentMessages != null) {
                currentMessages.add(binding.messageEditText.getText().toString());
                chatModel.messages.postValue(currentMessages);
                myAdapter.notifyItemInserted(currentMessages.size() - 1);
                binding.messageEditText.setText("");
            }
        });

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));

        binding.recycleView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                SentMessageBinding binding = SentMessageBinding.inflate(getLayoutInflater());
                return new MyRowHolder(binding.getRoot());
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                ArrayList<String> currentMessages = chatModel.messages.getValue();
                if (currentMessages != null) {
                    String obj = currentMessages.get(position);
                    holder.messageText.setText(obj);
                    holder.timeText.setText("");
                }
            }

            @Override
            public int getItemCount() {
                ArrayList<String> currentMessages = chatModel.messages.getValue();
                return currentMessages != null ? currentMessages.size() : 0;
            }


            @Override
            public int getItemViewType(int position) {
                return 0;
            }
        });

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
