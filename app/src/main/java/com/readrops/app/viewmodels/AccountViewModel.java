package com.readrops.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.readrops.app.database.entities.Account;
import com.readrops.app.repositories.ARepository;
import com.readrops.app.repositories.NextNewsRepository;

import io.reactivex.Single;

public class AccountViewModel extends AndroidViewModel {

    private ARepository repository;

    public AccountViewModel(@NonNull Application application) {
        super(application);

        repository = new NextNewsRepository(application);
    }

    public Single<Boolean> login(Account account) {
        return repository.login(account);
    }
}
