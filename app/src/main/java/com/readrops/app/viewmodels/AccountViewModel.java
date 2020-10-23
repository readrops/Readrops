package com.readrops.app.viewmodels;

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
import com.readrops.db.entities.account.AccountType;

import org.koin.core.parameter.DefinitionParametersKt;
import org.koin.java.KoinJavaComponent;

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

    public void setAccountType(AccountType accountType) {
        repository = KoinJavaComponent.get(ARepository.class, null,
                () -> DefinitionParametersKt.parametersOf(new Account(null, null, accountType)));
    }

    public void setAccount(Account account) {
        repository = KoinJavaComponent.get(ARepository.class, null,
                () -> DefinitionParametersKt.parametersOf(account));
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

    @SuppressWarnings("unchecked")
    public Single<Map<Folder, List<Feed>>> getFoldersWithFeeds() {
        return repository.getFoldersWithFeeds();
    }

    public Completable parseOPMLFile(Uri uri, Context context) {
        return OPMLParser.read(uri, context)
                .flatMapCompletable(foldersAndFeeds -> repository.insertOPMLFoldersAndFeeds(foldersAndFeeds));
    }
}
