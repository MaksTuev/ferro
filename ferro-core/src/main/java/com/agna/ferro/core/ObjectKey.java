/*
 * Copyright 2016 Maxim Tuev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agna.ferro.core;

/**
 * Key, used for store objects in {@link PersistentScreenScope}
 */
class ObjectKey {
    private Class clazz;
    private String tag;

    public ObjectKey(String tag) {
        this.tag = tag;
    }

    public ObjectKey(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectKey that = (ObjectKey) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        return !(tag != null ? !tag.equals(that.tag) : that.tag != null);

    }

    @Override
    public int hashCode() {
        int result = clazz != null ? clazz.hashCode() : 0;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
