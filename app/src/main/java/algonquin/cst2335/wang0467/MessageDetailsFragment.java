package algonquin.cst2335.wang0467;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

import algonquin.cst2335.wang0467.databinding.DetailsLayoutBinding;

public class MessageDetailsFragment extends Fragment {
    private ChatMessage selected;

    // Constructor that takes a ChatMessage object
    public MessageDetailsFragment(ChatMessage m) {
        selected = m;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DetailsLayoutBinding binding = DetailsLayoutBinding.inflate(inflater, container, false);

        binding.messageGoesHere.setText(selected.getMessage());
        binding.timeGoesHere.setText(selected.getTimeSent());
        binding.databaseIdGoesHere.setText("Id = " + selected.id);

        return binding.getRoot();
    }
}


