package com.example.test;

import android.content.Intent;
import java.lang.Object;
import com.example.test.MainActivity;
import com.example.test.MainActivity$$Aftermath;

public final class Aftermath {
    public static void onActivityResult(final Object target, final int requestCode, final int resultCode, final Intent data) {
        if(target instanceof MainActivity) {
            MainActivity$$Aftermath.onActivityResult((MainActivity) target, requestCode, resultCode, data);
        }
    }
}