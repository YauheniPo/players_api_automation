package com.automation.config

import org.aeonbits.owner.ConfigFactory

object ConfigProvider {
    val config: AppConfig by lazy {
        ConfigFactory.create(AppConfig::class.java)
    }
}
