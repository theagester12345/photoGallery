
package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

public class photoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFrag() {
        return photoGalleryFragment.newInstance();
    }
}

