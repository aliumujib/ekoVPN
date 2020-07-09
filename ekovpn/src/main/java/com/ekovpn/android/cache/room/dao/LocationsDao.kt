/*
 * Copyright 2020 Abdul-Mujeeb Aliu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ekovpn.android.cache.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ekovpn.android.cache.room.entities.LocationCacheModel
import com.ekovpn.android.cache.room.entities.ServerCacheModel

@Dao
interface LocationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(menus: List<LocationCacheModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(menu: LocationCacheModel)

    @Query("SELECT * FROM LocationCacheModel where country_code=:country_code")
    suspend fun getLocation(country_code: String): LocationCacheModel?

    @Query("SELECT * FROM LocationCacheModel where country=:country and city=:city")
    suspend fun getLocation(country: String, city:String): LocationCacheModel?

    @Query("DELETE FROM LocationCacheModel")
    fun deleteAll()

}
