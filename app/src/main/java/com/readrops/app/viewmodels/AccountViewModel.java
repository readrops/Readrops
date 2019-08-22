package com.readrops.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.database.entities.account.AccountType;
import com.readrops.app.repositories.ARepository;

import io.reactivex.Completable;
import io.reactivex.Single;

public class AccountViewModel extends AndroidViewModel {

    private ARepository repository;
    private Database database;

    public AccountViewModel(@NonNull Application application) {
        super(application);

        database = Database.getInstance(application);
    }

    public void setAccountType(AccountType accountType) throws Exception {
        repository = ARepository.repositoryFactory(null, accountType, getApplication());
    }

    public Single<Boolean> login(Account account, boolean insert) {
        return repository.login(account, insert);
    }

    public Single<Long> insert(Account account) {
        return Single.create(emitter -> {
            long id = database.accountDao().insert(account);
            emitter.onSuccess(id);
        });
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
}
