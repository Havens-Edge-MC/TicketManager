package com.github.hoshikurama.ticketmanager.commonse.database

import com.github.hoshikurama.ticketmanager.commonse.database.impl.CachedH2
import com.github.hoshikurama.ticketmanager.commonse.database.impl.H2
import com.github.hoshikurama.ticketmanager.commonse.database.impl.Memory
import com.github.hoshikurama.ticketmanager.commonse.database.impl.MySQL

interface DatabaseBuilder {
    fun build(): AsyncDatabase
}

class MySQLBuilder(
    private val host: String,
    private val port: String,
    private val databaseName: String,
    private val username: String,
    private val password: String,
) : DatabaseBuilder {
    override fun build() = MySQL(host, port, databaseName, username, password)
}

class MemoryBuilder(
    private val pathToFolder: String,
    private val backupFrequency: Long,
) : DatabaseBuilder {
    override fun build() = Memory(pathToFolder, backupFrequency)
}

class CachedH2Builder(
    private val absoluteFolderPath: String
) : DatabaseBuilder {
    override fun build() = CachedH2(absoluteFolderPath)
}

class H2Builder(
    private val absoluteFolderPath: String
) : DatabaseBuilder {
    override fun build() = H2(absoluteFolderPath)
}

class DatabaseBuilders(
    val mySQLBuilder: MySQLBuilder,
    val memoryBuilder: MemoryBuilder,
    val cachedH2Builder: CachedH2Builder,
    val h2Builder: H2Builder,
)
