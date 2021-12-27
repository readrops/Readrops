package com.readrops.api

import com.chimerapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor
import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.services.Credentials
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.api.services.fever.FeverService
import com.readrops.api.services.fever.adapters.*
import com.readrops.api.services.freshrss.FreshRSSDataSource
import com.readrops.api.services.freshrss.FreshRSSService
import com.readrops.api.services.freshrss.adapters.*
import com.readrops.api.services.nextcloudnews.NextNewsDataSource
import com.readrops.api.services.nextcloudnews.NextNewsService
import com.readrops.api.services.nextcloudnews.adapters.NextNewsFeedsAdapter
import com.readrops.api.services.nextcloudnews.adapters.NextNewsFoldersAdapter
import com.readrops.api.services.nextcloudnews.adapters.NextNewsItemsAdapter
import com.readrops.api.utils.AuthInterceptor
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val apiModule = module {

    single {
        OkHttpClient.Builder()
                .callTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.HOURS)
                .addInterceptor(get<AuthInterceptor>())
                .addInterceptor(NiddlerOkHttpInterceptor(get(), "niddler"))
                .build()
    }

    single { AuthInterceptor() }

    single { LocalRSSDataSource(get()) }

    //region freshrss

    factory { params -> FreshRSSDataSource(get(parameters = { params })) }

    factory { (credentials: Credentials) ->
        Retrofit.Builder()
                .baseUrl(credentials.url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get(named("freshrssMoshi"))))
                .build()
                .create(FreshRSSService::class.java)
    }

    single(named("freshrssMoshi")) {
        Moshi.Builder()
                .add(Types.newParameterizedType(List::class.java, Item::class.java), FreshRSSItemsAdapter())
                .add(Types.newParameterizedType(List::class.java, String::class.java), FreshRSSItemsIdsAdapter())
                .add(FreshRSSFeedsAdapter())
                .add(FreshRSSFoldersAdapter())
                .add(FreshRSSUserInfoAdapter())
                .build()
    }

    //endregion freshrss

    //region nextcloud news

    factory { params -> NextNewsDataSource(get(parameters = { params })) }

    factory { (credentials: Credentials) ->
        Retrofit.Builder()
                .baseUrl(credentials.url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get(named("nextcloudNewsMoshi"))))
                .build()
                .create(NextNewsService::class.java)
    }

    single(named("nextcloudNewsMoshi")) {
        Moshi.Builder()
                .add(NextNewsFeedsAdapter())
                .add(NextNewsFoldersAdapter())
                .add(Types.newParameterizedType(List::class.java, Item::class.java), NextNewsItemsAdapter())
                .build()
    }

    //endregion nextcloud news

    //region Fever

    factory { params -> FeverDataSource(get(parameters = { params })) }

    factory { (credentials: Credentials) ->
        Retrofit.Builder()
                .baseUrl(credentials.url)
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get(named("feverMoshi"))))
                .build()
                .create(FeverService::class.java)
    }

    single(named("feverMoshi")) {
        Moshi.Builder()
                .add(FeverFoldersAdapter())
                .add(FeverFeedsAdapter())
                .add(FeverItemsAdapter())
                .add(FeverFaviconsAdapter())
                .add(Boolean::class.java, FeverAPIAdapter())
                .add(FeverItemsIdsAdapter())
                .build()
    }

    //endregion Fever
}