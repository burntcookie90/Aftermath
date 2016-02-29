package com.example.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.michaelevans.aftermath.Aftermath;
import org.michaelevans.aftermath.OnActivityResult;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    static final int PICK_CONTACT_REQUEST = 1;
    static final int OTHER_REQUEST = 2;
    static final int GET_ACCOUNTS_PERMISSION_REQUEST = 1;

    @OnActivityResult(PICK_CONTACT_REQUEST)
    public void onContactPicked(int resultCode, Intent data) {
    }
}
