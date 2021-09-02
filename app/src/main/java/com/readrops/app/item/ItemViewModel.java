package com.readrops.app.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.readrops.app.repositories.ARepository;
import com.readrops.db.Database;
import com.readrops.db.entities.Item;
import com.readrops.db.entities.account.Account;
import com.readrops.db.pojo.ItemWithFeed;
import com.readrops.db.queries.ItemSelectionQueryBuilder;

import org.koin.core.parameter.DefinitionParametersKt;
import org.koin.java.KoinJavaComponent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.Completable;

public class ItemViewModel extends ViewModel {

    private final Database database;
    private Account account;

    public ItemViewModel(@NonNull Database database) {
        this.database = database;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LiveData<ItemWithFeed> getItemById(int id) {
        return database.itemDao().getItemById(ItemSelectionQueryBuilder.buildQuery(id,
                account.getConfig().getUseSeparateState()));
    }

    public Completable setStarState(Item item) {
        ARepository repository = KoinJavaComponent.get(ARepository.class, null, () -> DefinitionParametersKt.parametersOf(account));

        return repository.setItemStarState(item);
    }

    public Uri saveImageInCache(Bitmap bitmap, Context context) throws IOException {
        File imagesFolder = new File(context.getCacheDir().getAbsolutePath(), "images");

        if (!imagesFolder.exists())
            imagesFolder.mkdirs();

        File image = new File(imagesFolder, "shared_image.png");
        OutputStream stream = new FileOutputStream(image);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);

        stream.flush();
        stream.close();

        return FileProvider.getUriForFile(context, context.getPackageName(), image);
    }
}
