/*
 *    Copyright 2021 Google LLC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package moe.haruue.test.permission.consumer;

//import com.google.androidbrowserhelper.demos.customtabsheaders.pack.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ProtectedActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ACTION = Browser.EXTRA_HEADERS + ".action";
    private static String PERMISSION = "moe.haruue.test.permission.TEST";
    // This project is using a demo page hosted at Glitch. However, the demo will only work if the
    // Digital Asset Links validation is successful. So, when testing on your computer, remix the
    // project at https://glitch.com/edit/#!/custom-tabs-custom-he, edit the file under
    // `public/.well-known/assetlinks.json` with your own SHA-256 fingerprint (use Tools > Terminal
    // to find and edit the file), and update the URL below to the new project.
    private static final Uri URL = Uri.parse("https://www.cylog.org/headers/");

    private Button mExtraButton = null;

    private IntentFilter mIntentFilter = null;

    private BroadcastReceiver mReceiver = null;

    private Bundle mHeaders = null;

    @Override
    protected void onStart() {
        super.onStart();

        /*
        // Set up a callback that launches the intent after session was validated.
        CustomTabsCallback callback = new CustomTabsCallback() {
            @Override
            public void onRelationshipValidationResult(int relation, @NonNull Uri requestedOrigin,
                                                       boolean result, @Nullable Bundle extras) {
                // Can launch custom tabs intent after session was validated as the same origin.
                mExtraButton.setEnabled(true);
            }
        };

        // Set up a connection that warms up and validates a session.
        mConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(@NonNull ComponentName name,
                                                     @NonNull CustomTabsClient client) {
                // Create session after service connected.
                mSession = client.newSession(callback);
                client.warmup(0);
                // Validate the session as the same origin to allow cross origin headers.
                mSession.validateRelationship(CustomTabsService.RELATION_USE_AS_ORIGIN,
                        URL, null);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };

        //Add package names for other browsers that support Custom Tabs and custom headers.
        List<String> packageNames = Arrays.asList(
                "com.google.android.apps.chrome",
                "com.chrome.canary",
                "com.chrome.dev",
                "com.chrome.beta",
                "com.android.chrome",
                "org.mozilla.firefox",
                "org.mozilla.focus",
                "org.mozilla.focus.debug"
        );
        String packageName =
                CustomTabsClient.getPackageName(MainActivity.this, packageNames, false);
        if (packageName == null) {
            Toast.makeText(getApplicationContext(), "Package name is null.", Toast.LENGTH_SHORT)
                    .show();
        } else {
            // Bind the custom tabs service connection.
            Log.i(TAG, packageName);
            CustomTabsClient.bindCustomTabsService(this, packageName, mConnection);
        }
        */

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protected);

        if (PERMISSION.isEmpty()) {
            //PERMISSION += getString(R.string.permission_name);
        }

        mExtraButton = findViewById(R.id.btn_extra);
        //mExtraButton.setEnabled(false);
        mExtraButton.setOnClickListener(view -> {
            CustomTabsIntent intent = constructExtraHeadersIntent(null);
            if (intent != null) {
                intent.launchUrl(ProtectedActivity.this, URL);
            }
        });

        mHeaders = new Bundle();
        mHeaders.putString("redirect-url", "https://www.google.com/");
        mHeaders.putString("foo", "bar");

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION);
        mIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Log.i(TAG, "INTENT = " + intent.getAction());
                    if (ACTION.equals(intent.getAction())) {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(Browser.EXTRA_HEADERS + ".response");
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra(Browser.EXTRA_HEADERS, mHeaders);
                        sendBroadcast(broadcastIntent, PERMISSION);
                        if (mReceiver != null) {
                            unregisterReceiver(mReceiver);
                            mReceiver = null;
                        }
                    }
                }
            }
        };
        ContextCompat.registerReceiver(this, mReceiver, mIntentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service if we connected successfully and clear the session.
        /*if (mSession != null) {
            unbindService(mConnection);
            mConnection = null;
            mSession = null;
        }*/
        //mExtraButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "PERMISSION RESULT = " + PERMISSION);
        if (requestCode == 0) {
            Log.i(TAG, "PERMISSION CODE = " + requestCode);
            if (permissions.length == grantResults.length) {
                for (int idx = 0; idx < permissions.length; ++idx) {
                    Log.i(TAG, "PERMISSION = " + permissions[idx]);
                    if (PERMISSION.equals(permissions[idx]) && grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                        CustomTabsIntent intent = constructExtraHeadersIntent(null);
                        if (intent != null) {
                            intent.launchUrl(this, URL);
                        }
                        break;
                    } else {
                        Log.i(TAG, "PERMISSION GRANT = " + grantResults[idx]);
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private CustomTabsIntent constructExtraHeadersIntent(CustomTabsSession session) {
        Log.i(TAG, "PERMISSION RESULT = " + PERMISSION);
        if (ContextCompat.checkSelfPermission(ProtectedActivity.this, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProtectedActivity.this, new String[]{PERMISSION}, 0);
            return null;
        }

        CustomTabsIntent intent = null;
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (session != null) {
            builder.setSession(session);
        } else {
            builder.setShowTitle(false);
            builder.setUrlBarHidingEnabled(true);
        }

        // Example non-cors-whitelisted headers.
        intent = builder.build();
        intent.intent.putExtra(Browser.EXTRA_HEADERS, mHeaders);
        intent.intent.putExtra(ACTION, ACTION);
        intent.intent.putExtra(Browser.EXTRA_HEADERS + ".permission", PERMISSION);
        if (session == null) {
            intent.intent.putExtra(CustomTabsIntent.EXTRA_SESSION, (String) null);
        }

        return intent;
    }
}

