package com.readrops.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Account;
import com.readrops.app.repositories.ARepository;
import com.readrops.app.repositories.NextNewsRepository;

import io.reactivex.Completable;
import io.reactivex.Single;

public class AccountViewModel extends AndroidViewModel {

    private ARepository repository;
    private Database database;

    public AccountViewModel(@NonNull Application application) {
        super(application);

        repository = new NextNewsRepository(application);
        database = Database.getInstance(application);
    }

    public Single<Boolean> login(Account account) {
        return repository.login(account);
    }

    public Single<Long> insert(Account account) {
        return Single.create(emitter -> {
            long id = database.accountDao().insert(account);
            emitter.onSuccess(id);
        });

    }

    public Completable update(Account account) {
        return Completable.create(emitter -> {
            database.accountDao().update(account);
            emitter.onComplete();
        });
    }

    public Completable delete(Account account) {
        return Completable.create(emitter -> {
            database.accountDao().delete(account);
            emitter.onComplete();
        });
    }

    public Single<Integer> getAccountCountByAccountType(int accountTypeCode) {
        return Single.create(emitter -> emitter.onSuccess(database.accountDao().getAccountCountByType(accountTypeCode)));
    }

    public Single<Integer> getAccountCount() {
        return Single.create(emitter -> emitter.onSuccess(database.accountDao().getAccountCount()));
    }
}
