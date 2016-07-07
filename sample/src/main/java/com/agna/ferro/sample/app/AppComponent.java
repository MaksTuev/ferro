/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agna.ferro.sample.app;

import android.content.Context;

import com.agna.ferro.mvp.component.scope.PerApplication;
import com.agna.ferro.sample.module.book.BookRepository;

import dagger.Component;

/**
 * Application-level component
 */
@PerApplication
@Component(modules = AppModule.class)
public interface AppComponent {

    //Exposed to sub-graphs.
    Context context();

    BookRepository bookRepository();

}
