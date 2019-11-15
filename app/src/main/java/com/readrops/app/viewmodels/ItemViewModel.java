package com.readrops.app.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.readrops.app.database.Database;
import com.readrops.app.database.dao.ItemDao;
import com.readrops.app.database.pojo.ItemWithFeed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ItemViewModel extends AndroidViewModel {

    private ItemDao itemDao;

    public ItemViewModel(@NonNull Application application) {
        super(application);
        itemDao = Database.getInstance(application).itemDao();
    }

    public LiveData<ItemWithFeed> getItemById(int id) {
        return itemDao.getItemById(id);
    }


    public Uri saveImageInCache(Bitmap bitmap) throws IOException {
        File imagesFolder = new File(getApplication().getCacheDir().getAbsolutePath(), "images");

        if (!imagesFolder.exists())
            imagesFolder.mkdirs();

        File image = new File(imagesFolder, "shared_image.png");
        OutputStream stream = new FileOutputStream(image);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);

        stream.flush();
        stream.close();

        return FileProvider.getUriForFile(getApplication(), getApplication().getPackageName(), image);
    }
}
