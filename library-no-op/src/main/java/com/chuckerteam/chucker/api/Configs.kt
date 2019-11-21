package com.chuckerteam.chucker.api

class Configs private constructor(val localiseTime: Boolean = false) {

    class Builder {

        private var localisedTime: Boolean = false

        fun localisedTime(localisedTime: Boolean): Builder {
            this.localisedTime = localisedTime
            return this
        }

        fun build(): Configs {
            return Configs(localisedTime)
        }
    }
}
