package com.example.myapplication.presentation.group;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.data.onmate.AddMateItem;

import java.util.List;

public class AddMateViewModel extends ViewModel {
    private final MutableLiveData<List<AddMateItem>> _addedMates = new MutableLiveData<>();
    public LiveData<List<AddMateItem>> addedMates =  _addedMates;

    public void addMateToSelection(AddMateItem mateItem){
        List<AddMateItem> currentList = _addedMates.getValue();

        boolean isDuplicate = false;
        for(AddMateItem item: currentList){
            if(item.getUId().equals(mateItem.getUId())){
                isDuplicate = true;
                break;
            }
        }

        if(!isDuplicate){
            currentList.add(mateItem);
            _addedMates.setValue(currentList);
        }
    }

    public void removeMateFromSelection(AddMateItem mateItem){
        List<AddMateItem> currentList = _addedMates.getValue();

        currentList.remove(mateItem);
        _addedMates.setValue(currentList);
    }


}
