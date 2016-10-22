package nya.miku.wishmaster.ui.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import nya.miku.wishmaster.R;
import nya.miku.wishmaster.api.interfaces.CancellableTask;
import nya.miku.wishmaster.common.Async;
import nya.miku.wishmaster.common.Logger;
import nya.miku.wishmaster.common.MainApplication;
import nya.miku.wishmaster.lib.org_json.JSONArray;
import nya.miku.wishmaster.lib.org_json.JSONObject;
import nya.miku.wishmaster.ui.Database;
import nya.miku.wishmaster.ui.presentation.Subscriptions;

public class SettingsExporter {
    private static final String TAG = "SettingsExporter";
    
    @SuppressWarnings("unchecked")
    private static <T> T[] ListToArray(List<T> list, Class<?> itemClass) {
        return (T[]) list.toArray((T[]) Array.newInstance(itemClass, list.size()));
    }

    public static void Export(final File dir, final Activity activity) {
        final CancellableTask task = new CancellableTask.BaseCancellableTask();
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.app_settings_exporting));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                task.cancel();
            }
        });
        progressDialog.show();
        Async.runAsync(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put("version", MainApplication.getInstance().getPackageManager().getPackageInfo(MainApplication.getInstance().getPackageName(), 0).versionCode);
                } catch (PackageManager.NameNotFoundException e) {
                    Logger.e(TAG, e);
                    return;
                }
                json.put("history",
                        new JSONArray(
                                ListToArray(
                                        MainApplication.getInstance().database.getHistory(),
                                        Database.HistoryEntry.class
                                )
                        )
                );
                json.put("favorites",
                        new JSONArray(
                                ListToArray(
                                        MainApplication.getInstance().database.getFavorites(),
                                        Database.FavoritesEntry.class
                                )
                        )
                );
                json.put("hidden",
                        new JSONArray(
                                ListToArray(
                                        MainApplication.getInstance().database.getHidden(),
                                        Database.HiddenEntry.class
                                )
                        )
                );
                json.put("subscriptions",
                        new JSONArray(
                                ListToArray(
                                        MainApplication.getInstance().subscriptions.getSubscriptions(),
                                        Subscriptions.SubscriptionEntry.class
                                )
                        )
                );
                json.put("preferences", new JSONObject(MainApplication.getInstance().settings.getSharedPreferences()));
                if (task.isCancelled()) {
                    return;
                }
                File filename = new File(dir, "Overchan_settings_" + System.currentTimeMillis() + ".json");
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(filename);
                } catch (FileNotFoundException e) {
                    Logger.e(TAG, e);
                    return;
                }
                try {
                    outputStream.write(json.toString().getBytes());
                } catch (IOException e) {
                    Logger.e(TAG, e);
                    return;
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Logger.e(TAG, e);
                    return;
                }
                
                showMessage(activity.getString(R.string.app_settings_export_completed) + "\n" + filename.toString());
            }

            private void showMessage(final String message) {
                if (task.isCancelled()) return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (task.isCancelled()) return;
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            Logger.e(TAG, e);
                            return;
                        }
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                    }
                });
            }            
        });
    }
}
