package com.faendir.lightning_launcher.appdrawer;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by Lukas on 19.08.2015.
 */
@SuppressWarnings("unused")
public class LightningTask extends AsyncTask<Object, LightningTask.Item, Void> {

    Runnable preExecute;
    Runnable postExecute;
    Runnable update;
    Runnable background;
    Item item;

    @Override
    protected void onPreExecute() {
        if (preExecute != null) preExecute.run();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (postExecute != null) postExecute.run();
    }

    @Override
    protected void onProgressUpdate(Item... values) {
        setItem(values[0]);
        if (update != null) update.run();
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (background != null) {
            background.run();
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(){
        publishProgress(getItem());
    }

    public void setPreExecute(Runnable preExecute) {
        this.preExecute = preExecute;
    }

    public void setBackground(Runnable background) {
        this.background = background;
    }

    public void setPostExecute(Runnable postExecute) {
        this.postExecute = postExecute;
    }

    public void setUpdate(Runnable update) {
        this.update = update;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public static class Item {
        Intent intent;
        ResolveInfo info;
        Bitmap bitmap;
        int position;

        public Intent getIntent() {
            return intent;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public ResolveInfo getInfo() {
            return info;
        }

        public void setInfo(ResolveInfo info) {
            this.info = info;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}
