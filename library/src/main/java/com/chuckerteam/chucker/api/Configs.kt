package com.chuckerteam.chucker.api

/**
 * Configs are to store values for different chucker configurations
 * Eg: Show time in localised format(formatted considering the current timezone)
 * Dark mode (not available yet I guess). On or not
 * Configs like this can be built by the consumer app and Chucker will respect those configs and behaviour will be based on the config
 */
class Configs private constructor(val localiseTime: Boolean = false) {

    class Builder {

        private var localisedTime: Boolean = false

        fun localisedTime(localisedTime: Boolean): Builder{
            this.localisedTime = localisedTime
            return this
        }

        fun build(): Configs {

            return Configs(localisedTime)
        }
    }
}