package com.example.hazedetection.logic.location

import com.baidu.location.BDLocation

interface LocationCallback {
    fun onReceiveLocation(bdLocation: BDLocation)
}