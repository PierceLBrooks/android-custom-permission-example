package moe.haruue.test.permission.consumer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PACKAGE_NAME_PERMISSION_PROVIDER = "moe.haruue.test.permission.provider";
    private static final String PACKAGE_NAME_PERMISSION_CONSUMER = "moe.haruue.test.permission.consumer";
    private static final String ACTIVITY_NAME_PROTECTED_ACTIVITY = "moe.haruue.test.permission.consumer.ProtectedActivity";
    private static final String PERMISSION_TEST = "moe.haruue.test.permission.TEST";
    private static final int REQUEST_CODE_START_PROTECTED_ACTIVITY = 0x0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonPermission = findViewById(R.id.button_permission);
        buttonPermission.setOnClickListener(this);
        Button buttonSettings = findViewById(R.id.button_settings);
        buttonSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_permission) {
            if (isPackageInstalled(PACKAGE_NAME_PERMISSION_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, PERMISSION_TEST) == PackageManager.PERMISSION_GRANTED) {
                    startProtectedActivity();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{
                            PERMISSION_TEST
                    }, REQUEST_CODE_START_PROTECTED_ACTIVITY);
                }
            } else {
                Toast.makeText(this, "Please install the Perm Provider first", Toast.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.button_settings) {
            startApplicationDetailsSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_START_PROTECTED_ACTIVITY) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (PERMISSION_TEST.equals(permission)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        startProtectedActivity();
                    } else if (grantResult == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Unknown permission grant result: " + grantResult, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void startProtectedActivity() {
        Intent startIntent = new Intent();
        startIntent.setComponent(new ComponentName(PACKAGE_NAME_PERMISSION_CONSUMER, ACTIVITY_NAME_PROTECTED_ACTIVITY));
        startActivity(startIntent);
    }

    private void startApplicationDetailsSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingsIntent.setData(Uri.parse("package:" + getPackageName()));
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(settingsIntent);
    }

    private boolean isPackageInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        Log.i(TAG, "PACKAGE = " + packageName);
        Log.i(TAG, "MANAGER = " + pm);
        boolean result = false;
        try {
            PackageInfo pi = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pi = pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0));
            } else {
                pi = pm.getPackageInfo(packageName, 0);
            }
            result = pi != null;
            Log.i(TAG, "INFO = " + pi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
