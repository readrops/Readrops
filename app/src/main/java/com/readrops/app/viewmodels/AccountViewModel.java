package com.readrops.app.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.readrops.api.opml.OPMLParser;
import com.readrops.app.repositories.ARepository;
import com.readrops.db.Database;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.account.Account;
import com.readrops.db.entities.account.AccountType;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;

public class AccountViewModel extends AndroidViewModel {

    private static final String TAG = AccountViewModel.class.getSimpleName();

    private ARepository repository;
    private Database database;

    public AccountViewModel(@NonNull Application application) {
        super(application);

        database = Database.getInstance(application);
    }

    public void setAccountType(AccountType accountType) throws Exception {
        repository = ARepository.repositoryFactory(null, accountType, getApplication());
    }

    public void setAccount(Account account) {
        try {
            repository = ARepository.repositoryFactory(account, getApplication());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public Single<Boolean> login(Account account, boolean insert) {
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

    public Single<Map<Folder, List<Feed>>> getFoldersWithFeeds() {
        return repository.getFoldersWithFeeds();
    }

    public Completable parseOPMLFile(Uri uri) {
        return OPMLParser.read(uri, getApplication())
                .flatMapCompletable(foldersAndFeeds -> repository.insertOPMLFoldersAndFeeds(foldersAndFeeds));
    }
}
