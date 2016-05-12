/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.listing;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 07/12/2015.
 * TODO: fill description
 */
public class SimplePagingProvider<T> extends AbstractSimplePagingProvider<T> {

    @NonNull
    private final PagesProvider<T> pagesProvider;

    public SimplePagingProvider(@NonNull final PagesProvider<T> pagesProvider) {
        this(pagesProvider, DEFAULT_PAGE_SIZE);
    }

    public SimplePagingProvider(@NonNull final PagesProvider<T> pagesProvider, final int pageSize) {
        super(pageSize);
        this.pagesProvider = pagesProvider;
    }

    @Override
    protected Observable<List<T>> observeItems(final int indexOfPage) {
        return pagesProvider.loadPage(indexOfPage * getPageSize(), getPageSize()).map(ArrayList::new);
    }

    public interface PagesProvider<T> {

        @NonNull
        Observable<Collection<T>> loadPage(int offset, int limit);

    }

}
