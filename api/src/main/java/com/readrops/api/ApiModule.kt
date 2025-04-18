package com.readrops.api

import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.services.Credentials
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.api.services.fever.FeverService
import com.readrops.api.services.fever.adapters.FeverAPIAdapter
import com.readrops.api.services.fever.adapters.FeverFaviconsAdapter
import com.readrops.api.services.fever.adapters.FeverFeeds
import com.readrops.api.services.fever.adapters.FeverFeedsAdapter
import com.readrops.api.services.fever.adapters.FeverFoldersAdapter
import com.readrops.api.services.fever.adapters.FeverItemsAdapter
import com.readrops.api.services.fever.adapters.FeverItemsIdsAdapter
import com.readrops.api.services.greader.GReaderDataSource
import com.readrops.api.services.greader.GReaderService
import com.readrops.api.services.greader.adapters.FreshRSSUserInfoAdapter
import com.readrops.api.services.greader.adapters.GReaderFeedsAdapter
import com.readrops.api.services.greader.adapters.GReaderFoldersAdapter
import com.readrops.api.services.greader.adapters.GReaderItemsAdapter
import com.readrops.api.services.greader.adapters.GReaderItemsIdsAdapter
import com.readrops.api.services.nextcloudnews.NextcloudNewsDataSource
import com.readrops.api.services.nextcloudnews.NextcloudNewsService
import com.readrops.api.services.nextcloudnews.adapters.NextcloudNewsFeedsAdapter
import com.readrops.api.services.nextcloudnews.adapters.NextcloudNewsFoldersAdapter
import com.readrops.api.services.nextcloudnews.adapters.NextcloudNewsItemsAdapter
import com.readrops.api.utils.AuthInterceptor
import com.readrops.api.utils.ErrorInterceptor
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val apiModule = module {

    single {
        OkHttpClient.Builder()
            .callTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<ErrorInterceptor>())
            //.addInterceptor(NiddlerOkHttpInterceptor(get(), "niddler"))
            .build()
    }

    single { AuthInterceptor() }

    single { ErrorInterceptor() }

    single { LocalRSSDataSource(get()) }

    //region greader/freshrss

    factory { params -> GReaderDataSource(get(parameters = { params })) }

    factory { (credentials: Credentials) ->
        Retrofit.Builder()
            .baseUrl(credentials.url)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get(named("greaderMoshi"))))
            .build()
            .create(GReaderService::class.java)
    }

    single(named("greaderMoshi")) {
        Moshi.Builder()
                .add(Types.newParameterizedType(List::class.java, Item::class.java), GReaderItemsAdapter())
                .add(Types.newParameterizedType(List::class.java, String::class.java), GReaderItemsIdsAdapter())
                .add(GReaderFeedsAdapter())
                .add(GReaderFoldersAdapter())
                .add(FreshRSSUserInfoAdapter())
                .build()
    }

    //endregion greader/freshrss

    //region nextcloud news

    factory { params -> NextcloudNewsDataSource(get(parameters = { params })) }

    factory { (credentials: Credentials) ->
        Retrofit.Builder()
                .baseUrl(credentials.url)
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get(named("nextcloudNewsMoshi"))))
                .build()
                .create(NextcloudNewsService::class.java)
    }

    single(named("nextcloudNewsMoshi")) {
        Moshi.Builder()
                .add(NextcloudNewsFeedsAdapter())
                .add(NextcloudNewsFoldersAdapter())
                .add(Types.newParameterizedType(List::class.java, Item::class.java), NextcloudNewsItemsAdapter())
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
                .add(FeverFeeds::class.java, FeverFeedsAdapter())
                .add(FeverItemsAdapter())
                .add(FeverFaviconsAdapter())
                .add(Boolean::class.java, FeverAPIAdapter())
                .add(FeverItemsIdsAdapter())
                .build()
    }

    //endregion Fever
}