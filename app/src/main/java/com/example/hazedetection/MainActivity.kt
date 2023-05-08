package com.example.hazedetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.example.hazedetection.databinding.ActivityMainBinding
import com.example.hazedetection.logic.location.LocationCallback
import com.example.hazedetection.logic.location.MyLocationListener
import com.example.hazedetection.logic.model.Location
import com.example.hazedetection.logic.model.Weather
import com.example.hazedetection.logic.model.getSky
import com.example.hazedetection.ui.weather.WeatherViewModel
import com.example.hazedetection.util.ChartUtil.showChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale

class MainActivity : AppCompatActivity(), LocationCallback {
    //权限数组
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //请求权限意图
    private val requestPermissionIntent =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocation =
                java.lang.Boolean.TRUE == result[Manifest.permission.ACCESS_FINE_LOCATION]
            val writeStorage =
                java.lang.Boolean.TRUE == result[Manifest.permission.WRITE_EXTERNAL_STORAGE]
            if (fineLocation && writeStorage) {
                //权限已经获取到，开始定位
                startLocation()
            }
        }
    private fun requestPermission() {
        //因为项目的最低版本API是23，所以肯定需要动态请求危险权限，只需要判断权限是否拥有即可
        if (checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
        ) {
            //开始权限请求
            requestPermissionIntent.launch(permissions)
            return
        }
        //开始定位
        startLocation()
    }
    private lateinit var mLocationClient: LocationClient
    private fun startLocation() {
        if (mLocationClient != null) {
            mLocationClient.start()
        }
    }
    private val myListener = MyLocationListener()
    private fun initLocation() {
        try {
            mLocationClient = LocationClient(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mLocationClient != null) {
            myListener.setCallback(this)
            //注册定位监听
            mLocationClient.registerLocationListener(myListener)
            val option = LocationClientOption()
            //如果开发者需要获得当前点的地址信息，此处必须为true
            option.setIsNeedAddress(true)
            //可选，设置是否需要最新版本的地址信息。默认不需要，即参数为false
            option.setNeedNewVersionRgc(true)
            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            mLocationClient.locOption = option
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }

        setContentView(binding.root)
        initLocation()
        requestPermission()
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
        binding.swipeRefresh.setColorSchemeResources(R.color.purple_200)
        binding.swipeRefresh.setOnRefreshListener {
            startLocation()
            refreshWeather()
        }
    }
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        binding.swipeRefresh.isRefreshing = true
        Toast.makeText(this, "刷新成功", Toast.LENGTH_SHORT).show()
    }
    private fun showWeatherInfo(weather: Weather) {
        binding.nowXml.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        binding.nowXml.currentTemp.text = currentTempText
        binding.nowXml.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        binding.nowXml.currentAQI.text = currentPM25Text
        binding.nowXml.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        // 填充air_quility.xml布局中的数据
        val air_quility = realtime.airQuality
        binding.airQuilityXml.des.text = realtime.airQuality.description.chn
        binding.airQuilityXml.aqi.text = air_quility.aqi.chn.toInt().toString()
        binding.airQuilityXml.CO.text = air_quility.co.toString()
        binding.airQuilityXml.pm10.text = air_quility.pm10.toString()
        binding.airQuilityXml.pm25.text = air_quility.pm25.toString()
        binding.airQuilityXml.so2.text = air_quility.so2.toString()
        binding.airQuilityXml.NO2.text = air_quility.no2.toString()
        binding.airQuilityXml.O3.text = air_quility.o3.toString()
        // 填充forecast.xml布局中的数据
        binding.forecastXml.forecastLayout.removeAllViews()
        val dataList1 = ArrayList<Entry>()
        val dataList2 = ArrayList<Entry>()
        val xLabels = ArrayList<String>()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val humidity = daily.humidity[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                binding.forecastXml.forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            binding.forecastXml.forecastLayout.addView(view)

            dataList1.add(Entry(i.toFloat(),temperature.avg))
            dataList2.add(Entry(i.toFloat(),humidity.avg * 100 ) )
            xLabels.add(SimpleDateFormat("MM-dd", Locale.getDefault()).format(skycon.date))
        }
        showChart(this@MainActivity, binding.linechart1.lineChart, xLabels, dataList1, "", "平均温度", "℃")
        showChart(this@MainActivity, binding.linechart2.lineChart, xLabels, dataList2, "", "平均湿度", "%")

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        binding.lifeIndexXml.coldRiskText.text = lifeIndex.coldRisk[0].desc
        binding.lifeIndexXml.dressingText.text = lifeIndex.dressing[0].desc
        binding.lifeIndexXml.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        binding.lifeIndexXml.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
    }

    override fun onReceiveLocation(bdLocation: BDLocation) {
        val latitude = bdLocation.latitude //获取纬度信息
        val longitude = bdLocation.longitude //获取经度信息
        val radius = bdLocation.radius //获取定位精度，默认值为0.0f
        val coorType = bdLocation.coorType
        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
        val errorCode = bdLocation.locType //161  表示网络定位结果
        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
        val addr = bdLocation.addrStr //获取详细地址信息
        val country = bdLocation.country //获取国家
        val province = bdLocation.province //获取省份
        val city = bdLocation.city //获取城市
        val district = bdLocation.district //获取区县
        val street = bdLocation.street //获取街道信息
        val locationDescribe = bdLocation.locationDescribe //获取位置描述信息
        viewModel.locationLng = longitude.toString()
        viewModel.locationLat = latitude.toString()
        viewModel.placeName = city.toString()
        refreshWeather()
    }

}