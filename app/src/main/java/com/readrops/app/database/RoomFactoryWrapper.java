package com.readrops.app.database;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

/**
 * Workaround class to avoid item recycler view scrolling down when updating data
 * This class is to keep until a new version of androidx paging is released with
 * bug https://issuetracker.google.com/issues/123834703 merged.
 * @param <T>
 */
public class RoomFactoryWrapper<T> extends DataSource.Factory<Integer, T> {
    final DataSource.Factory<Integer, T> m_wrappedFactory;

    public RoomFactoryWrapper(@NonNull DataSource.Factory<Integer, T> wrappedFactory) {
        m_wrappedFactory = wrappedFactory;
    }

    @NonNull
    @Override
    public DataSource<Integer, T> create() {
        return new DataSourceWrapper<>((PositionalDataSource<T>) m_wrappedFactory.create());
    }

    public static class DataSourceWrapper<T> extends PositionalDataSource<T> {
        final PositionalDataSource<T> m_wrappedSource;

        DataSourceWrapper(PositionalDataSource<T> wrappedSource) {
            m_wrappedSource = wrappedSource;
        }

        @Override
        public void addInvalidatedCallback(@NonNull InvalidatedCallback onInvalidatedCallback) {
            m_wrappedSource.addInvalidatedCallback(onInvalidatedCallback);
        }

        @Override
        public void removeInvalidatedCallback(
                @NonNull InvalidatedCallback onInvalidatedCallback) {
            m_wrappedSource.removeInvalidatedCallback(onInvalidatedCallback);
        }

        @Override
        public void invalidate() {
            m_wrappedSource.invalidate();
        }

        @Override
        public boolean isInvalid() {
            return m_wrappedSource.isInvalid();
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams params,
                                @NonNull LoadInitialCallback<T> callback) {
            // Workaround for paging bug: https://issuetracker.google.com/issues/123834703
            // edit initial load position to start 1/2 load ahead of requested position
            int newStartPos = params.placeholdersEnabled
                    ? params.requestedStartPosition
                    : Math.max(0, params.requestedStartPosition - (params.requestedLoadSize / 2));
            m_wrappedSource.loadInitial(new LoadInitialParams(
                    newStartPos,
                    params.requestedLoadSize,
                    params.pageSize,
                    params.placeholdersEnabled
            ), callback);
        }

        @Override
        public void loadRange(@NonNull LoadRangeParams params,
                              @NonNull LoadRangeCallback<T> callback) {
            m_wrappedSource.loadRange(params, callback);
        }
    }
}