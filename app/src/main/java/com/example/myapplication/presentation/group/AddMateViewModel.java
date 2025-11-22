package com.example.myapplication.presentation.group;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.data.onmate.AddMateItem;

import java.util.ArrayList;
import java.util.List;

public class AddMateViewModel extends ViewModel {
    private final MutableLiveData<List<AddMateItem>> _addedMates = new MutableLiveData<>();
    public LiveData<List<AddMateItem>> addedMates =  _addedMates;

    public boolean addMateToSelection(AddMateItem mateItem){
        List<AddMateItem> currentList = _addedMates.getValue();

        if (currentList == null) {
            currentList = new ArrayList<>();
        }

        // 중복 체크
        for (AddMateItem mate : currentList) {
            if (mate.getUId().equals(mateItem.getUId())) {
                return false; // 중복
            }
        }

        List<AddMateItem> newList = new ArrayList<>(currentList);
        newList.add(mateItem);
        _addedMates.setValue(newList);
        return true; // 성공
    }
    public void removeMateFromSelection(AddMateItem mateItem){
        List<AddMateItem> currentList = _addedMates.getValue();

        currentList.remove(mateItem);
        _addedMates.setValue(currentList);
    }


}
