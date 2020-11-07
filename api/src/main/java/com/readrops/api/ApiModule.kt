package com.readrops.api

import com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor
import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.services.freshrss.FreshRSSDataSource
import com.readrops.api.services.freshrss.FreshRSSService
import com.readrops.api.services.freshrss.adapters.FreshRSSFeedsAdapter
import com.readrops.api.services.freshrss.adapters.FreshRSSFoldersAdapter
import com.readrops.api.services.freshrss.adapters.FreshRSSItemsAdapter
import com.readrops.api.services.freshrss.adapters.FreshRSSItemsIdsAdapter
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

const val BASE_URL = "https://baseurl.com"

val apiModule = module {

    single(createdAtStart = true) {
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

    single { FreshRSSDataSource(get()) }

    single {
        get<Retrofit>(named("freshrssRetrofit"))
                .create(FreshRSSService::class.java)
    }

    single(named("freshrssRetrofit")) {
        Retrofit.Builder() // url will be set dynamically in AuthInterceptor
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(get<OkHttpClient>())
                .addConverterFactory(MoshiConverterFactory.create(get<Moshi>(named("freshrssMoshi"))))
                .build()
    }

    single(named("freshrssMoshi")) {
        Moshi.Builder()
                .add(Types.newParameterizedType(List::class.java, Item::class.java), FreshRSSItemsAdapter())
                .add(Types.newParameterizedType(List::class.java, String::class.java), FreshRSSItemsIdsAdapter())
                .add(FreshRSSFeedsAdapter())
                .add(FreshRSSFoldersAdapter())
                .build()
    }

    //endregion freshrss

    //region nextcloud news

    single { NextNewsDataSource(get()) }

    single {
        get<Retrofit>(named("nextcloudNewsRetrofit"))
                .create(NextNewsService::class.java)
    }

    single(named("nextcloudNewsRetrofit")) {
        Retrofit.Builder() // url will be set dynamically in AuthInterceptor
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(get<OkHttpClient>())
                .addConverterFactory(MoshiConverterFactory.create(get<Moshi>(named("nextcloudNewsMoshi"))))
                .build()
    }

    single(named("nextcloudNewsMoshi")) {
        Moshi.Builder()
                .add(NextNewsFeedsAdapter())
                .add(NextNewsFoldersAdapter())
                .add(Types.newParameterizedType(List::class.java, Item::class.java), NextNewsItemsAdapter())
                .build()
    }

    //endregion nextcloud news
}