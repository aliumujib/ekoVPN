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
import com.ekovpn.android.cache.room.entities.ServerCacheModel
import com.ekovpn.android.cache.room.entities.ServerLocationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ServersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(menus: List<ServerCacheModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(menu: ServerCacheModel)

    @Query("SELECT * FROM ServerCacheModel where serverId=:id")
    suspend fun getServer(id: String): ServerCacheModel?

    @Query("SELECT ServerCacheModel.*,  LocationCacheModel.* FROM ServerCacheModel LEFT OUTER JOIN LocationCacheModel on ServerCacheModel.location == LocationCacheModel.locationId where protocol=:protocol")
    fun getServersForProtocol(protocol: String): Flow<List<ServerLocationModel>>

    @Query("DELETE FROM ServerCacheModel")
    fun deleteAll()

}
