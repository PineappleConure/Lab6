package algonquin.cst2335.wang0467;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ChatRoomViewModel extends ViewModel {
    public MutableLiveData<ArrayList<ChatMessage>> messages = new MutableLiveData<>();
    public MutableLiveData<ChatMessage> selectedMessage = new MutableLiveData<>();
    MutableLiveData<Integer> selectedMessagePosition = new MutableLiveData<>();
}
