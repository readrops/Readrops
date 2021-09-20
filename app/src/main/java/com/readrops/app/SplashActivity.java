package com.readrops.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.readrops.app.account.AccountTypeListActivity;
import com.readrops.app.account.AccountViewModel;
import com.readrops.app.itemslist.MainActivity;

import org.koin.android.compat.ViewModelCompat;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private AccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        viewModel = ViewModelCompat.getViewModel(this, AccountViewModel.class);

        viewModel.getAccountCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Integer>() {
                    @Override
                    public void onSuccess(Integer count) {
                        if (count > 0) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Intent intent = new Intent(getApplicationContext(), AccountTypeListActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }
                });


        /*PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .addTag(SyncWorker.Companion.getTAG())
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SyncWorker.Companion.getTAG(), ExistingPeriodicWorkPolicy.REPLACE, request);*/
    }
}
