package algonquin.cst2335.wang0467;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

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
    public MutableLiveData<Integer> selectedMessagePosition = new MutableLiveData<>();
    private ChatMessageDAO mDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar myToolbar = binding.myToolbar;
        setSupportActionBar(myToolbar);

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

        chatModel.selectedMessage.observe(this, newMessageValue -> {
            if (newMessageValue != null) {
                MessageDetailsFragment chatFragment = new MessageDetailsFragment(newMessageValue);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentLocation, chatFragment)
                        .addToBackStack("")
                        .commit();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_1) {
            showDeleteMessageDialog();;
            return true;
        } else if (item.getItemId() == R.id.about) {
            Toast.makeText(this, "Version 1.0, created by Linna Wang", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this message?")
                .setTitle("Confirm")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialog, which) -> deleteSelectedMessage())
                .show();
    }

    private void deleteSelectedMessage() {
        Integer position = chatModel.selectedMessagePosition.getValue();
        if (position != null) {
            ChatMessage messageToDelete = chatModel.messages.getValue().get(position);

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                mDAO.deleteMessage(messageToDelete);
                runOnUiThread(() -> {
                    ArrayList<ChatMessage> currentMessages = chatModel.messages.getValue();
                    currentMessages.remove(position.intValue());
                    myAdapter.notifyItemRemoved(position);
                });
            });
        }
    }


    private void addMessageToModel(ChatMessage newMessage) {
        Executor thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            // Insert the message
            mDAO.insertMessage(newMessage);

            // Fetch the last inserted message
            ChatMessage insertedMessage = mDAO.getLastInsertedMessage();

            // Update UI on the main thread
            runOnUiThread(() -> {
                ArrayList<ChatMessage> currentMessages = chatModel.messages.getValue();
                if (currentMessages != null) {
                    currentMessages.add(insertedMessage);
                    chatModel.messages.postValue(currentMessages);
                    myAdapter.notifyItemInserted(currentMessages.size() - 1);
                }
            });
        });
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

            itemView.setOnClickListener(click -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (chatModel.messages.getValue() != null) {
                        ChatMessage selected = chatModel.messages.getValue().get(position);
                        chatModel.selectedMessage.postValue(selected);
                        chatModel.selectedMessagePosition.postValue(position);
                    }
                }
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
