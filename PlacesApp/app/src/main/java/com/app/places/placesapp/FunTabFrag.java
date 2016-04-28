package com.app.places.placesapp;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Fragment displayed when the Fun Tab is selected
 */
public class FunTabFrag extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.tabs, container, false);
    }

    public void updateView(ArrayList<Place> fun) {
        // The array to be displayed
        ArrayList<String> output = new ArrayList<String>();

        for (int i = 0; i < fun.size(); i ++) {
            Place currPlace = fun.get(i);
            String spaces = "";
            for (int j = 0; j < (40 - currPlace.getName().length()); j ++) {
                spaces = spaces + " ";
            }
            output.add(i, currPlace.getName() + spaces + currPlace.getAddress());
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,output);
        setListAdapter(adapter);
    }
}
