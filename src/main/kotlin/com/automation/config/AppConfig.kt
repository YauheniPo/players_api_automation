package com.automation.config

import org.aeonbits.owner.Config
import org.aeonbits.owner.Config.DefaultValue
import org.aeonbits.owner.Config.Key
import org.aeonbits.owner.Config.LoadPolicy
import org.aeonbits.owner.Config.LoadType
import org.aeonbits.owner.Config.Sources

// LoadType.MERGE — читаем все источники, первый найденный выигрывает.
// FIRST (дефолт) берёт первый доступный источник целиком, из-за чего config.properties игнорируется.
@LoadPolicy(LoadType.MERGE)
@Sources(
    "system:env",
    "system:properties",
    "classpath:config.local.properties",
    "classpath:config.properties",
)
interface AppConfig : Config {
    @Key("base.url")
    @DefaultValue("")
    fun baseUrl(): String

    @Key("admin.login")
    @DefaultValue("login")
    fun adminLogin(): String

    @Key("admin.password")
    @DefaultValue("")
    fun adminPassword(): String
}
