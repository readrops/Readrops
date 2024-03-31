package com.readrops.app.account;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.readrops.api.opml.OPMLParser;
import com.readrops.app.repositories.ARepository;
import com.readrops.db.Database;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.account.Account;

import org.koin.core.parameter.ParametersHolderKt;
import org.koin.java.KoinJavaComponent;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;

public class AccountViewModel extends ViewModel {

    private ARepository repository;
    private final Database database;

    public AccountViewModel(@NonNull Database database) {
        this.database = database;
    }

    public void setAccount(Account account) {
        repository = KoinJavaComponent.get(ARepository.class, null,
                () -> ParametersHolderKt.parametersOf(account));
    }

    public Completable login(Account account, boolean insert) {
        setAccount(account);
        return repository.login(account, insert);
    }

    public Single<Long> insert(Account account) {
        return database.accountDao().insert(account);
    }

    public Completable update(Account account) {
        return database.accountDao().update(account);
    }

    public Completable delete(Account account) {
        return database.accountDao().delete(account);
    }

    public Single<Integer> getAccountCount() {
        return database.accountDao().getAccountCount();
    }

    @SuppressWarnings("unchecked")
    public Single<Map<Folder, List<Feed>>> getFoldersWithFeeds() {
        return repository.getFoldersWithFeeds();
    }

    public Completable parseOPMLFile(Uri uri, Context context) throws FileNotFoundException {
        /*return OPMLParser.read(context.getContentResolver().openInputStream(uri))
                .flatMapCompletable(foldersAndFeeds -> repository.insertOPMLFoldersAndFeeds(foldersAndFeeds));*/
        return Completable.complete();
    }
}
