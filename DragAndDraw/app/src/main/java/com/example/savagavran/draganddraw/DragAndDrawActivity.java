package com.example.savagavran.draganddraw;

import android.support.v4.app.Fragment;
import android.os.Bundle;

public class DragAndDrawActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return DragAndDrawFragment.newInstance();
    }
}
