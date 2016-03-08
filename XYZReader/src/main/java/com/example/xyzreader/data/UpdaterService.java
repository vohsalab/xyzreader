package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.example.xyzreader.remote.RSSNewsItem;
import com.example.xyzreader.remote.RemoteEndpointUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            BlockingQueue<List<RSSNewsItem>> blockingQueue = RemoteEndpointUtil.fetchItems();
            ArrayList<RSSNewsItem> itemsList = (ArrayList<RSSNewsItem>) blockingQueue.take();

            for (RSSNewsItem i : itemsList) {
                ContentValues values = new ContentValues();

                values.put(ItemsContract.Items.SERVER_ID, i.getId());
                values.put(ItemsContract.Items.AUTHOR, i.getAuthor());
                values.put(ItemsContract.Items.TITLE, i.getTitle());
                values.put(ItemsContract.Items.BODY, i.getBody());
                values.put(ItemsContract.Items.THUMB_URL, i.getThumb());
                values.put(ItemsContract.Items.PHOTO_URL, i.getPhoto());
                values.put(ItemsContract.Items.ASPECT_RATIO, i.getAspectRatio());
                time.parse3339(i.getPublishedDate());
                values.put(ItemsContract.Items.PUBLISHED_DATE, time.toMillis(false));
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);

        } catch (RemoteException | InterruptedException | OperationApplicationException e) {
            Log.e(TAG, "Error updating content.", e);
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }
}
