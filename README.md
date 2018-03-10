# **概述**

百度地图模块源码（含iOS和Android）

APICloud 的 bMap 模块是对百度地图移动端开放 SDK 进行的一次封装。目的是为了让 APICloud 的广大开发者只需用 html+js 即可快速、高效的集成百度移动端地图到自己的 App 内。所以需在百度开放平台提供的 SDK 基础上，按照 APICloud 平台的模块开发规范，对百度地图的 SDK 提供的接口进行一层封装。本模块只对百度地图的常用接口进行了封装，其中涉及到 UI 的部分功能无法完全彻底的封装。因此开源此模块源码，原生开发者可以在此模块的基础上继续完善该模块的其它接口。比如扩展地图上添加自定义气泡的接口，让前端开发者很快地在 APICloud 上开发出各式各样、效果炫酷的 App。

# **模块接口文档**


/*
Title: bMap
Description: bMap
*/

<p style="color: #ccc; margin-bottom: 30px;">来自于：APICloud 官方<a style="background-color: #95ba20; color:#fff; padding:4px 8px;border-radius:5px;margin-left:30px; margin-bottom:0px; font-size:12px;text-decoration:none;" target="_blank" href="//www.apicloud.com/mod_detail/bMap">立即使用</a></p>

## 基础类

<div class="outline">

[initMapSDK](#initMapSDK)
[open](#open)
[close](#close)
[show](#show)
[hide](#hide)
[setRect](#setRect)
[getLocation](#getLocation)
[stopLocation](#stopLocation)
[getLocationServices](#getLocationServices)
[getCoordsFromName](#getCoordsFromName)
[getNameFromCoords](#getNameFromCoords)
[getDistance](#getDistance)
[showUserLocation](#showUserLocation)
[setCenter](#setCenter)
[getCenter](#getCenter)
[setZoomLevel](#setZoomLevel)
[getZoomLevel](#getZoomLevel)
[setMaxAndMinZoomLevel](#setMaxAndMinZoomLevel)
[getShowMapPoi](#getShowMapPoi)
[setShowMapPoi](#setShowMapPoi)
[setMapAttr](#setMapAttr)
[setRotation](#setRotation)
[setOverlook](#setOverlook)
[setScaleBar](#setScaleBar)
[setCompass](#setCompass)
[setCompass](#setCompass)
[setHeatMap](#setHeatMap)
[setBuilding](#setBuilding)
[setRegion](#setRegion)
[getRegion](#getRegion)
[transCoords](#transCoords)
[zoomIn](#zoomIn)
[zoomOut](#zoomOut)
[isPolygonContantsPoint](#isPolygonContantsPoint)
[addEventListener](#addEventListener)
[removeEventListener](#removeEventListener)
[startSearchGPS](#startSearchGPS)
[stopSearchGPS](#stopSearchGPS)
[getCurrentLocation](#getCurrentLocation)

</div>


## 室内地图

<div class="outline">

[setIndoorMap](#setIndoorMap)
[addIndoorListener](#addIndoorListener)
[switchIndoorMapFloor](#switchIndoorMapFloor)
[indoorSearch](#indoorSearch)

</div>

## 标注、气泡类

<div class="outline">

[addAnnotations](#addAnnotations)
[getAnnotationCoords](#getAnnotationCoords)
[setAnnotationCoords](#setAnnotationCoords)
[annotationExist](#annotationExist)
[setBubble](#setBubble)
[popupBubble](#popupBubble)
[closeBubble](#closeBubble)
[addBillboard](#addBillboard)
[addMobileAnnotations](#addMobileAnnotations)
[moveAnnotation](#moveAnnotation)
[removeAnnotations](#removeAnnotations)

</div>

## 覆盖物类

<div class="outline">

[addLine](#addLine)
[addPolygon](#addPolygon)
[addArc](#addArc)
[addCircle](#addCircle)
[addImg](#addImg)
[removeOverlay](#removeOverlay)

</div>

## 搜索类

<div class="outline">

[searchRoute](#searchRoute)
[drawRoute](#drawRoute)
[removeRoute](#removeRoute)
[searchBusRoute](#searchBusRoute)
[drawBusRoute](#drawBusRoute)
[removeBusRoute](#removeBusRoute)
[searchInCity](#searchInCity)
[searchNearby](#searchNearby)
[searchInBounds](#searchInBounds)  
[autocomplete](#autocomplete)

</div>

## 离线地图类

<div class="outline">

[getHotCityList](#getHotCityList)
[getOfflineCityList](#getOfflineCityList)
[searchCityByName](#searchCityByName)
[getAllUpdateInfo](#getAllUpdateInfo)
[getUpdateInfoByID](#getUpdateInfoByID)
[start](#start)
[update](#update)
[pause](#pause)
[remove](#remove)  
[addOfflineListener](#addOfflineListener)
[removeOfflineListener](#removeOfflineListener)

</div>

# **概述**

**百度地图简介**

百度地图是百度提供的一项网络地图搜索服务，覆盖了国内近400个城市、数千个区县。在百度地图里，用户可以查询街道、商场、楼盘的地理位置，也可以找到离您最近的所有餐馆、学校、银行、公园等等。2010年8月26日，在使用百度地图服务时，除普通的电子地图功能之外，新增加了三维地图按钮。

**百度地图特色功能**

- 智能查询，出行无忧：
拥有强大的路线查询及规划能力，告别迷路可能。从A到B，总能给出最佳线路及打车费用，还有N条备选方案。支持公交、驾车、步行、地铁四种出行方式；随时随地查看实时路况，街道真实全景图和室内图。

- 导航精准，零罚单：
语音搜索功能，帮助告别繁琐的手动输入，让您开车更安全。
路况播报，实时播报您周围路况动态，随时清晰掌握每一条道路的路况及电子眼预报，不再为罚单发愁。
步行也能导航！结合街道全景，精彩一步到位。

- 权威数据，免费下载：
覆盖行业最全、最准的地点信息，提供海量资源免费下载。离线也能看地图，离线包瘦身90%，支持在线更新，更快捷更省流量！导航资源数据包，免费下载路口3D+卫星版实景图。

- 附近吃喝玩乐，商务预订，一网打尽：
提供附近美食、酒店、电影、购物、打车、外卖、景点、银行等海量商户信息，包括商户电话、地址、地图、点评，一键规划路线，在线预订；免费下载优惠券，还可享受最新鲜的团购折扣信息。

**模块概述**

bMap 模块封装了百度地图的原生 SDK，集成了百度地图常用基本接口；手机版原生地图，不同于 js 地图，相对于js地图而言，本模块封装的原生手机地图更加流畅迅速、动画效果更加逼真。使用此模块可轻松把百度地图集成到自己的app内，实现百度地图常用的定位、关键字搜索、周边搜索、自定义标注及气泡、查公交路线等各种功能；另外本模块已支持百度地图离线版本。

若某些带UI的接口不能满足开发设计需求，开发者（借助于原生开发者）可在本模块基础上修改少量原生代码，随心所欲的自定义百度地图所具有的原生功能，简单、轻松、快捷、高效、迅速集成百度地图，将自己的 app 和百度地图实现无缝链接。

**模块使用攻略**

***注意事项***

- 本模块内带动画效果的接口不可同时调用（两个以上），需要设置延迟（`setTimeout`）处理。
- bMap 模块是 baiduMap 模块的优化版。不可与baiduMap、aMapNavigation, aMap模块同时使用
- **需要在APICloud 网站控制台编译界面选择定位权限。**
- 使用本模块需云编译安装包，或以自定义 loader 的形式使用
- 离线地图功能属于“基础地图”这个功能模块，开发者使用时请注意选择

***使用此模块之前必须先配置  config 文件，配置方法如下：***

- 名称：bMap
- 参数：android_api_key、ios_api_key
- 备注：同一个 App 需要同时支持 iOS 和 Android 平台，必须单独申请各自的 apiKey，并同时配置在 config 文件中
- 配置示例:

```xml
  <feature name="bMap">
    <param name="android_api_key" value="f7Is0dWLom2q6rV3ZfFPZ1aa" />
    <param name="ios_api_key" value="81qz3dBYB5q2nGji4IYrawr1" />
  </feature>
```

- 字段描述:

    **android_api_key**：在百度地图开放平台申请的 Android 端 AK

    **ios_api_key**：在百度地图开放平台申请的 iOS 端 AK
 
 ***百度ak申请方法见[百度地图接入指南](//docs.apicloud.com/APICloud/开放平台接入指南/baidu),[申请百度key注意事项](https://community.apicloud.com/bbs/forum.php?mod=viewthread&tid=20073&extra=page%3D3%26filter%3Dtypeid%26typeid%3D63)***
 
 ***注意：在使用搜索类接口时，请确保您的 ak 是通过百度认证的状态，否则会报异常***
 
 **iOS如果需要自定义当前位置图标需要自定义模块**

制作方法如下：下载 bMap 模块 zip 包并解压（请到[APICloud网站](https://docs.apicloud.com/Client-API/Open-SDK/bMap)下载），将自己的图片放到 zip 包内 target 目录下的 mapapi.bundle里的images目录内。然后重新压缩为 zip 包文件上传自定义模块，云编译时勾选该模块。请到[APICloud网站](https://docs.apicloud.com/Client-API/Open-SDK/bMap)下载安卓平台模块包。
 
##**模块接口**

<div id="initMapSDK"></div>

# **initMapSDK**

初始化百度地图引擎，**本接口仅支持 iOS 平台，android平台不需要初始化**

使用场景：

当开发者尚未调用 open 接口，直接调用 getLocation、getCoordsFromName、getNameFromCoords、getDistance 以及搜索类接口时，需要首先调用此接口初始化地图引擎，然后在本接口的回调内调用检索相关接口（getLocation、getCoordsFromName、getNameFromCoords、getDistance 以及搜索类），以提高检索成功率。


initMapSDK(callback(ret, err))


## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true	      //布尔类型；是否初始化成功，true||false
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 0	      //数字类型；错误码，取值范围如下：
                   //-300：链接服务器错误
                   //-200：服务返回数据异常
                   //0：授权验证通过
                   //101：ak不存在
                   //102：mcode签名值不正确
                   //200:APP不存在，AK有误请检查再重试
                   //201:APP被用户自己禁用，请在控制台解禁
                   //更多错误码参考：http://lbsyun.baidu.com/index.php?title=lbscloud/api/appendix
}
```

## 示例代码

```js
var map = api.require('bMap');
map.initMapSDK(function(ret) {
    if (ret.status) {
        alert('地图初始化成功，可以从百度地图服务器检索信息了！');
    }
});
```

## 可用性

iOS系统

可提供的1.0.4及更高版本


<div id="open"></div>

# **open**

打开百度地图

open({params}, callback(ret))

## params

rect：

- 类型：JSON 对象
- 描述：（可选项）模块的位置及尺寸
- 内部字段：

```js
{
    x: 0,   //（可选项）数字类型；地图左上角的 x 坐标（相对于所属的 Window 或 Frame）；默认：0
    y: 0,   //（可选项）数字类型；地图左上角的 y 坐标（相对于所属的 Window 或 Frame）；默认：0
    w: 320, //（可选项）数字类型；地图的宽度；默认：所属的 Window 或 Frame 的宽度
    h: 480  //（可选项）数字类型；地图的高度；默认：所属的 Window 或 Frame 的高度
}
```

center：

- 类型：数字
- 描述：（可选项）打开地图时设置的中心点经纬度，若不传则默认打开北京市为中心的地图
- 内部字段：

```js
{
    lon: 116.213,       //数字类型；打开地图时设置的中心点经度
    lat: 39.213         //数字类型；打开地图时设置的中心点纬度
}
```

zoomLevel：

- 类型：数字
- 描述：（可选项）设置百度地图缩放等级，取值范围：3-18级
- 默认值：10

showUserLocation：

- 类型：布尔
- 描述：（可选项）是否在地图上显示用户位置
- 默认值：true

fixedOn：

- 类型：字符串类型
- 描述：（可选项）模块视图添加到指定 frame 的名字（只指 frame，传 window 无效）
- 默认：模块依附于当前 window

fixed:

- 类型：布尔
- 描述：（可选项）模块是否随所属 window 或 frame 滚动
- 默认值：true（不随之滚动）

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true	 //布尔型；true||false
}
```

## 示例代码

```js
var map = api.require('bMap');
map.open({
    rect: {
        x: 0,
        y: 0,
        w: 320,
        h: 300
    },
    center: {
        lon: 116.4021310000,
        lat: 39.9994480000
    },
    zoomLevel: 10,
    showUserLocation: true,
    fixedOn: api.frameName,
    fixed: true
}, function(ret) {
    if (ret.status) {
        alert('地图打开成功');
    }
});

```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="close"></div>

# **close**

关闭百度地图

close()

## 示例代码

```js
var map = api.require('bMap');
map.close();
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="show"></div>

# **show**

显示百度地图

show()

## 示例代码

```js
var map = api.require('bMap');
map.show();
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="hide"></div>

# **hide**

隐藏百度地图

hide()

## 示例代码

```js
var map = api.require('bMap');
map.hide();
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setRect"></div>

# **setRect**

重设地图的显示区域

setRect({params})

## params

rect：

- 类型：JSON 对象
- 描述：（可选项）模块的位置及尺寸
- 内部字段：

```js
{
    x: 0,   //（可选项）数字类型；地图左上角的 x 坐标（相对于所属的 Window 或 Frame）；默认：原值
    y: 0,   //（可选项）数字类型；地图左上角的 y 坐标（相对于所属的 Window 或 Frame）；默认：原值
    w: 320, //（可选项）数字类型；地图的宽度；默认：原值
    h: 480  //（可选项）数字类型；地图的高度；默认：原值
}
```

## 示例代码

```js
var map = api.require('bMap');
map.setRect({
    rect: {
        x: 0,
        y: 0,
        w: 320,
        h: 300
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getLocation"></div>

# **getLocation**

开始定位，若要支持后台定位需配置 [config.xml](/APICloud/技术专题/app-config-manual) 文件 location 字段，**无需调用 open 接口即可定位。在 android 平台上，离线定位功能需要手动打开GPS，并在无遮挡物的室外**

getLocation({params}, callback(ret, err))

## params

accuracy：

- 类型：字符串
- 描述：（可选项）定位精度
- 默认值：'100m'
- 取值范围：
    - 10m
    - 100m
    - 1km
    - 3km

autoStop：

- 类型：布尔
- 描述：（可选项）获取到位置信息后是否自动停止定位
- 默认值：true

filter：

- 类型：数字
- 描述：（可选项）位置更新所需的最小距离（单位米），autoStop 为 true 时，此参数有效
- 默认值：1.0

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,               //布尔型；true||false
    lon: 116.213,               //数字类型；经度
    lat: 39.213,                //数字类型；纬度
    accuracy: 65,               //数字类型；本次定位的精度，仅支持 iOS 平台
    timestamp: 1396068155591,    //数字类型；时间戳
	locationType:netWork        //字符串；定位类型；GPS||NetWork||OffLine(仅限Android)
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 0,         //数字类型；错误码
    msg: ''          //字符串类型；错误信息说明
}
```

## 示例代码

```js
var bMap = api.require('bMap');
bMap.getLocation({
    accuracy: '100m',
    autoStop: true,
    filter: 1
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    } else {
        alert(err.code);
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="stopLocation"></div>

# **stopLocation**

停止定位

stopLocation()

## 示例代码

```js
var bMap = api.require('bMap');
bMap.stopLocation();
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getLocationServices"></div>

# **getLocationServices**

获取定位是否开启，及当前 app 获取的定位权限

getLocationServices(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    enable: true,               //布尔型；当前设备定位功能是否开启，true|false；在Android 平台上本参数表示当前app定位功能是否可用；在iOS 平台上本参数表示当前设备定位功能是否可用，若判断当前app的定位功能，可用authorizationStatus识别。
	authorizationStatus:        //字符串；当前 App 获取的定位权限，Android 平台无此参数。取值范围如下：
	                            //notDetermined：用户从未选择过权限
	                            //restricted：未授权，且用户无法更新，如家长控制情况下
	                            //denied：用户拒绝该应用使用定位服务
	                            //always：总是使用
	                            //whenInUse：按需使用
}
```

## 示例代码

```js
var bMap = api.require('bMap');
bMap.getLocationServices(function(ret, err) {
    if (ret.enable) {
        alert(JSON.stringify(ret));
    } else {
        alert("未开启定位功能！");
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getCoordsFromName"></div>

# **getCoordsFromName**

根据地址查找经纬度，**无需调用 open 接口即可使用**

getCoordsFromName({params}, callback(ret, err))

## params

city：

- 类型：字符串
- 描述：（可选项）地址所在城市

address：

- 类型：字符串
- 描述：地址信息

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,        //布尔型；true||false
    lon: 116.351,        //数字类型；地址所在经度
    lat: 39.283          //数字类型；地址所在纬度
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getCoordsFromName({
    city: '北京',
    address: '天安门'
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getNameFromCoords"></div>

# **getNameFromCoords**

根据经纬度查找地址信息，**无需调用 open 接口即可使用**

getNameFromCoords({params}, callback(ret, err))

## params

lon：

- 类型：数字
- 描述：经度

lat：

- 类型：数字
- 描述：纬度

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,              //布尔型；true||false
    province: '',              //字符串类型；省份
    city: '',                  //字符串类型；城市
    district: '',              //字符串类型；县区
    streetName: '',            //字符串类型；街道名
    streetNumber: '',          //字符串类型；街道号
    country:'',                //字符串类型；国家
    countryCode:'',            //字符串类型；国家代码
    adCode:'',                 //字符串类型；行政区域编码
    businessCircle:'',         //字符串类型；商圈名称
    sematicDescription:'',     //字符串类型；结合当前位置POI的语义化结果描述
    cityCode:'',               //字符串类型；城市编码
    lon: 116.351,              //数字类型；经度
    lat: 39.283,               //数字类型；纬度
    address: '',               //字符串类型；地址信息
    poiList:[{                 //数组类型；经纬度点热点列表
       name: '',               //字符串类型；热点名称
       uid: '',                //字符串类型；热点id
       address: '',            //字符串类型；热点地址
       city: '',               //字符串类型；热点所在城市
       phone: '',              //字符串类型；热点电话
       postcode: '',           //字符串类型；热点邮编
       epoitype: '',           //字符串类型；热点类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路
       coord: {                //JSON对象；热点坐标信息
          lat: ,               //数字类型；热点纬度
          lon:                 //数字类型；热点经度
       }
    }]
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getNameFromCoords({
    lon: 116.384767,
    lat: 39.989539
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getDistance"></div>

# **getDistance**

获取地图两点之间的距离，**无需调用 open 接口即可使用**

getDistance({params}, callback(ret))

## params

start：

- 类型：JSON 对象
- 描述：起点经纬度
- 内部字段：

```js
{
    lon: 106.486654,    //数字类型；起点的经度
    lat: 29.490295      //数字类型；起点的纬度
}
```

end：

- 类型：JSON 对象
- 描述：终点经纬度
- 内部字段：

```js
{
    lon: 106.581515,    //数字类型；终点的经度
    lat: 29.615467      //数字类型；终点的纬度
}
```

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,              //布尔型；true||false
    distance: 16670.90         //数字类型；两点之间的距离，单位：米
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getDistance({
    start: {
        lon: 106.486654,
        lat: 29.490295
    },
    end: {
        lon: 106.581515,
        lat: 29.615467
    }
}, function(ret) {
    if (ret.status) {
        alert(ret.distance);
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="showUserLocation"></div>

# **showUserLocation**

是否在地图上显示用户位置，**会自动移动地图可视区域中心点到用户当前坐标位置，自带地图移动动画效果**

showUserLocation({params})

## params

isShow：

- 类型：布尔
- 描述：（可选项）是否显示用户位置
- 默认值：true

trackingMode：

- 类型：字符串
- 描述：（可选项）用户当前位置显示形式
- 默认值：none
- 取值范围：
    - none（标准模式）注：Android平台为指向箭头，iOS平台为圆点
    - follow（跟踪模式）
    - compass（罗盘模式）
    
imageName：

- 类型：字符串
- 描述：（可选项）自定义当前位置图标的图片名称 (android不支持)
- 注意：使用此模块需要自定义模块，参见“概述”内容
- 默认值：百度地图默认当前位置图标

imagePath:

- 类型：字符串
- 描述：(可选项)当前位置显示图标的图片，支持fs,widget (ios不支持)
- 默认值：百度地图默认当前位置图标

## 示例代码

```js
var map = api.require('bMap');
map.showUserLocation({
    isShow: true,
    trackingMode: 'none'
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setCenter"></div>

# **setCenter**

根据经纬度设置百度地图中心点，**此接口可带动画效果**

setCenter({params})

## params

coords：

- 类型：JSON 对象
- 描述：中心点的经纬度
- 内部字段：

```js
{
    lon: 116.404,       //数字类型；设置中心点的经度
    lat: 39.915         //数字类型；设置中心点的纬度
}
```

animation：

- 类型：布尔类型
- 描述：（可选项）设置地图的中心点时，是否带动画效果
- 默认：true

## 示例代码

```js
var map = api.require('bMap');
map.setCenter({
    coords: {
        lon: 116.404,
        lat: 39.915
    },
    animation: false
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getCenter"></div>

# **getCenter**

获取百度地图中心点坐标

getCenter(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    lon: 116.404,       //数字类型；地图中心点的经度
    lat: 39.915         //数字类型；地图中心点的纬度
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getCenter(function(ret) {
    alert(ret.lon + '*' + ret.lat);
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setZoomLevel"></div>

# **setZoomLevel**

设置百度地图缩放等级，**此接口自带动画效果**

setZoomLevel({params})

## params

level：

- 类型：数字
- 描述：（可选项）地图比例尺级别，取值范围：3-18级
- 默认值：10

## 示例代码

```js
var map = api.require('bMap');
map.setZoomLevel({
    level: 10
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getZoomLevel"></div>

# **getZoomLevel**

获取百度地图比例尺级别，取值范围：3-18级

getZoomLevel(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    level: 10,       //数字类型；地图比例尺级别，取值范围：3-18级
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getZoomLevel(function(ret) {
    alert(ret.level);
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本


<div id="setMaxAndMinZoomLevel"></div>

# **setMaxAndMinZoomLevel**

设置最大缩放比例，取值范围：3-18级

setMaxAndMinZoomLevel({params})

## params

maxLevel：

- 类型：数字
- 描述：（可选项）设置的最大缩放比例
- 默认：15

minLevel：

- 类型：数字
- 描述：（可选项）设置的最小缩放比例
- 默认：10

## 示例代码

```js
var map = api.require('bMap');
map.setMaxAndMinZoomLevel({
   maxLevel: 15,
	minLevel:10
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本


<div id="setShowMapPoi"></div>

# **setShowMapPoi**

设定地图是否显示底图 poi 标注(不包含室内图标注)

setShowMapPoi({params})

## params

showMapPoi：

- 类型：布尔
- 描述：（可选项）是否显示地图 poi
- 默认值：true

## 示例代码

```js
var map = api.require('bMap');
map.setShowMapPoi({
    showMapPoi: true
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getShowMapPoi"></div>

# **getShowMapPoi**

获取地图是否显示底图 poi 标注(不包含室内图标注)，Android系统（不支持）

getShowMapPoi(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    showMapPoi: true       //布尔类型；是否显示地图 poi
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getShowMapPoi(function(ret) {
    alert(ret.showMapPoi);
});
```

## 可用性

iOS系统，Android系统（不支持）

可提供的1.0.0及更高版本


<div id="setMapAttr"></div>

# **setMapAttr**

设置百度地图相关属性

setMapAttr({params})

## params

type:

- 类型：字符串
- 描述：（可选项）设置地图类型
- 默认值：'standard'
- 取值范围：
    - standard（标准地图）
    - trafficOn（打开实时路况）
    - trafAndsate（实时路况和卫星地图）
    - satellite（卫星地图）

zoomEnable：

- 类型：布尔
- 描述：（可选项）捏合手势是否可以缩放地图
- 默认值：true

scrollEnable：

- 类型：布尔
- 描述：（可选项）拖动手势是否可以移动地图
- 默认值：ture

rotateEnabled：

- 类型：布尔
- 描述：（可选项）拖动手势是否可以旋转地图
- 默认值：ture

overlookEnabled：

- 类型：布尔
- 描述：（可选项）拖动手势是否可以改变地图俯视角度
- 默认值：ture

## 示例代码

```js
var map = api.require('bMap');
map.setMapAttr({
    type: 'standard'
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setRotation"></div>

# **setRotation**

设置百度地图旋转角度，**此接口自带动画效果**

setRotation({params})

## params

degree：

- 类型：数字
- 描述：（可选项）地图旋转角度，取值范围：-180° - 180°
- 默认值：0

## 示例代码

```js
var map = api.require('bMap');
map.setRotation({
    degree: 30
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setOverlook"></div>

# **setOverlook**

设置百度地图俯视角度，**此接口自带动画效果**

setOverlook({params})

## params

degree：

- 类型：数字
- 描述：（可选项）地图俯视角度，取值范围：-45° - 0°
- 默认值：0

## 示例代码

```js
var map = api.require('bMap');
map.setOverlook({
    degree: -30
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setScaleBar"></div>

# **setScaleBar**

设置百度地图比例尺

setScaleBar({params})

## params

show：

- 类型：布尔
- 描述：（可选项）是否显示比例尺
- 默认值：false

position：

- 类型：JSON 对象
- 描述：（可选项）比例尺的位置，设定坐标以地图左上角为原点
- 内部字段：
```js
{ 
    x: 0,   //（可选项）数字类型；比例尺左上角的 x 坐标（相对于地图）；默认：0
    y: 0    //（可选项）数字类型；比例尺左上角的 y 坐标（相对于地图）；默认：0
}
```

## 示例代码

```js
var map = api.require('bMap');
map.setScaleBar({
    show: true,
    position: {
        x: 100,
        y: 100
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setCompass"></div>

# **setCompass**

设置百度地图指南针位置，**只有地图旋转或视角变化时才显示指南针**

setCompass({params})

## params

position：

- 类型：JSON 对象
- 描述：（可选项）指南针的位置，设定坐标以地图左上角为原点
- 内部字段：
```js
{ 
    x: 0,   //（可选项）数字类型；指南针中心点的 x 坐标（相对于地图）；默认：指南针宽度/2.0
    y: 0    //（可选项）数字类型；指南针中心点的 y 坐标（相对于地图）；默认：指南针高度/2.0
}
```

## 示例代码

```js
var map = api.require('bMap');
map.setCompass({
    position: {
        x: 100,
        y: 100
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setTraffic"></div>

# **setTraffic**

设置百度地图交通路况

setTraffic({params})

## params

traffic：

- 类型：布尔
- 描述：（可选项）是否显示交通路况
- 默认值：true

## 示例代码

```js
var map = api.require('bMap');
map.setTraffic({
    traffic: true
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setHeatMap"></div>

# **setHeatMap**

设置百度地图城市热力图

setHeatMap({params})

## params

heatMap：

- 类型：布尔
- 描述：（可选项）是否显示城市热力图
- 默认值：true

## 示例代码

```js
var map = api.require('bMap');
map.setHeatMap({
    heatMap: true
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setBuilding"></div>

# **setBuilding**

设定地图是否现实 3D 楼块效果，**地图放大，才会有 3D 楼快效果，倾斜视角 3D 效果会更明显**

setBuilding({params})

## params

building：

- 类型：布尔
- 描述：（可选项）是否现实3D楼块效果
- 默认值：true

## 示例代码

```js
var map = api.require('bMap');
map.setBuilding({
    building: true
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setRegion"></div>

# **setRegion**

设置地图显示范围（矩形区域），**此接口可带动画效果**

setRegion({params})

## params

lbLon：

- 类型：数字
- 描述：矩形区域左下角的经度

lbLat：

- 类型：数字
- 描述：矩形区域左下角的纬度

rtLon：

- 类型：数字
- 描述：矩形区域右上角的经度

rtLat：

- 类型：数字
- 描述：矩形区域右上角的纬度

animation：

- 类型：布尔类型
- 描述：（可选项）设置地图的区域时，是否带动画效果
- 默认：true

## 示例代码

```js
var map = api.require('bMap');
map.setRegion({
    lbLon: 116.027143, 
    lbLat: 39.772348, 
    rtLon: 116.832025, 
    rtLat: 40.126349,
    animation: true
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getRegion"></div>

# **getRegion**

获取地图显示范围（矩形区域）

getRegion(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,                //布尔型；true||false
    lbLon: 116.027143,           //数字类型；矩形区域左下角的经度
    lbLat: 39.772348,            //数字类型；矩形区域左下角的纬度
    rtLon: 116.832025,           //数字类型；矩形区域右上角的经度
    rtLat: 40.126349             //数字类型；矩形区域右上角的纬度    
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getRegion(function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="transCoords"></div>

# **transCoords**

将其它类型的地理坐标转换为百度坐标。**无需调用 open 接口即可使用**

transCoords({params}, callback(ret, err))

## params

type：

- 类型：字符串
- 描述：原始地理坐标类型
- 默认值：common
- 取值范围：
     - gps（GPS设备采集的原始GPS坐标）
     - common（google、soso、aliyun、mapabc、amap和高德地图所用坐标）

lon：

- 类型：数字
- 描述：原始地理坐标经度

lat：

- 类型：数字
- 描述：原始地理坐标纬度

mcode：

- 类型：字符串
- 描述：到[百度地图开放平台](http://lbsyun.baidu.com/apiconsole/key)获取的安全码（Android端），点击应用的设置按钮 -> 设置界面 -> 安全码（数字签名+;+包名）

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,  //布尔型；true||false
    lon: 116.213,  //数字类型；转换后的百度地理坐标经度
    lat: 39.213    //数字类型；转换后的百度地理坐标纬度
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1         //数字类型；错误码
                    //1：（参数非法）
                    //2：（转换失败）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.transCoords({
    type: "common",
    lon: 116.351,
    lat: 39.283,
    mcode: '0B:13:25:D7:85:46:0A:67:12:F3:29:88:64:56:63:10:7A:9C:C4:59;com.apicloud.A6985734480360'
}, function(ret, err) {
    alert(JSON.stringify(ret));
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="zoomIn"></div>

# **zoomIn**

缩小地图，放大视角，放大一级比例尺，**此接口自带动画效果**

zoomIn()

## 示例代码

```js
var map = api.require('bMap');
map.zoomIn();
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="zoomOut"></div>

# **zoomOut**

放大地图，缩小视角，缩小一级比例尺，**此接口自带动画效果**

zoomOut()

## 示例代码

```js
var map = api.require('bMap');
map.zoomOut();
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addEventListener"></div>

# **addEventListener**

监听地图相关事件

addEventListener({params}, callback(ret))

## params

name:

- 类型：字符串
- 描述：地图相关事件名称
- 取值范围：
    - longPress（长按事件）
    - viewChange（地图视角范围改变事件）
    - click（单击事件）
    - dbclick（双击事件）
    - zoom（放大缩小事件）iOS 平台暂不支持此监听

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    lon: 116.351,           //数字类型；触发事件的地点的经度（longPress，click，dbclick），地图中心的经度（viewChange，zoom）
    lat: 39.283,            //数字类型；触发事件的地点的纬度（longPress，click，dbclick），地图中心的经度（viewChange，zoom）
    zoom: 15,               //数字类型；地图缩放角度
    rotate: 30,             //数字类型；地图旋转角度
    overlook: 30            //数字类型；视角倾斜度
	zoomType: 'zoomIn'      //字符串类型；zoomIn 放大，zoomOut缩小；name为zoom时有值
}
```

## 示例代码

```js
var map = api.require('bMap');
map.addEventListener({
    name: 'longPress'
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="isPolygonContantsPoint"></div>

# **isPolygonContantsPoint**

判断已知点是否在指定的多边形区域内

isPolygonContantsPoint({params}, callback(ret))

## params

point：

- 类型：JSON 对象
- 描述：已知点的地理坐标
- 内部字段：

```js
{
    lon: 116.297,      //数字类型；经度
    lat: 40.109        //数字类型；纬度
}
```

points：

- 类型：数组
- 描述：多边形的各个点组成的数组
- 内部字段：

```js
[{
    lon: 116.297,      //数字类型；经度
    lat: 40.109        //数字类型；纬度
}]
```
## callBack(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true      //布尔类型；目标点是否在指定区域内，true || false      
}
```


## 示例代码

```js
var map = api.require('bMap');
map.isPolygonContantsPoint({
    point: {
        lon: 116.39432327,
        lat: 39.98963192
    },
    points: [{
        lon: 116.39432327,
        lat: 39.98963192
    }, {
        lon: 116.49432328,
        lat: 39.98963192
    }, {
        lon: 116.39432327,
        lat: 39.88933191
    }]
}, function(ret) {
    alert(ret.status);
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本


<div id="removeEventListener"></div>

# **removeEventListener**

停止监听地图相关事件

removeEventListener({params})

## params

name:

- 类型：字符串
- 描述：地图相关事件名称
- 取值范围：
    - longPress（长按事件）
    - viewChange（地图视角范围改变事件）
    - click（单击事件）
    - dbclick（双击事件）
    - zoom（放大缩小事件）

## 示例代码

```js
var map = api.require('bMap');
map.removeEventListener({
    name: 'longPress'
});

```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="startSearchGPS"></div>

# **startSearchGPS**

开始搜索GPS信息（卫星个数，以及每个卫星的信噪比数组），本接口仅支持 android 平台

startSearchGPS(callback(ret, err))

## callBack

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,         //布尔类型；true || false，为false时可能是未开启GPS
	satelliteCount: 3,    //数字类型； 当前能搜索到的卫星数
	snrArray: [20,30,23]  //数组类型；各个卫星的信噪比 
}
```

## 示例代码

```js
var map = api.require('bMap');
map.startSearchGPS(
    function(ret) {
        if (ret.status) {
            alert(ret.satelliteCount);
        }
    }
);
```

## 可用性

Android系统

可提供的1.0.0及更高版本

<div id="stopSearchGPS"></div>

# **stopSearchGPS**

停止搜索GPS信息，本接口仅支持 android 平台

stopSearchGPS()

## 示例代码

```js
var map = api.require('bMap');
map.stopSearchGPS();
```

## 可用性

Android系统

可提供的1.0.0及更高版本

<div id="getCurrentLocation"></div>

# **getCurrentLocation**

获取当前定位，**需调用 open 接口，且showUserLocation为true时，才可定位。**

getCurrentLocation(callback(ret))

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,               //布尔类型；是否获取成功，true|false
    lon: 116.213,               //数字类型；经度
    lat: 39.213,                //数字类型；纬度
    updating: false,            //布尔类型；是否正在更新位置信息，暂仅支持 iOS 平台
    title: '',                  //字符串类型；当前位置的标题信息，暂仅支持 iOS 平台
    subtitle: '',               //字符串类型；当前位置的子标题信息，暂仅支持 iOS 平台
    headInfo: {                 //JSON 对象；当前位置方向信息，暂仅支持 iOS 平台
         magnetic: 20,          //数字类型；地磁方向角度，范围：0.0 - 359.9，0表示正北
         trueHeading: 10,       //数字类型；地理方向角度，范围：0.0 - 359.9，0表示正北
         accuracy: 10           //数字类型；方向精度，Represents the maximum deviation of where the magnetic heading may differ from the actual geomagnetic heading in degrees. A negative value indicates an invalid heading
    }
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1               //数字类型；错误码，取值如下：
                          // 1：尚未定位成功
                          //-1：未知错误
}
```

## 示例代码

```
var bMap = api.require('bMap');
bMap.getCurrentLocation(
function(ret, err) {
	if (ret.status){
      alert(JSON.stringify(ret));
	}else {
      alert(JSON.stringify(err));
	}
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本


<div id="setIndoorMap"></div>

# **setIndoorMap**

打开关闭室内地图

**注意**

1.因路况、卫星图和城市热力图，仅支持20级地图数据显示，室内地图放大到22级，打开路况、卫星图或城市热力图，无相应数据显示。

2.室内图默认是关闭的，通过本接口打开

setIndoorMap({params})

## params

draggable：

- 类型：布尔
- 描述：（可选项）是否打开室内地图
- 默认值：true


## 示例代码

```js
var map = api.require('bMap');
map.setIndoorMap({
    enable: true
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.9及更高版本

<div id="addIndoorListener"></div>

# **addIndoorListener**

添加进出室内地图的监听

addIndoorListener(callback(ret))


## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    enter: true,           //布尔型；进出室内地图，true：进入
    strID: ‘’,             //字符串类型；室内ID
    strFloor:‘’,           //字符串类型；当前楼层
}
```

## 示例代码

```js
var map = api.require('bMap');
map.addIndoorListener(function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.9及更高版本


<div id="switchIndoorMapFloor"></div>

# **switchIndoorMapFloor**

切换楼层

switchIndoorMapFloor({params},callback(ret,err))

## params

strID：

- 类型：字符串
- 描述：室内ID

strFloor：

- 类型：字符串
- 描述：楼层


## callback(ret,err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,         //布尔类型；是否设置成功
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1                //数字类型；错误码
                           //1:切换楼层失败
                           //2:地图还未聚焦到传入的室内图
                           //3:当前室内图不存在该楼层
}
```

## 示例代码

```js
var map = api.require('bMap');
map.switchIndoorMapFloor({
    strFloor:'',
    strID:''
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.9及更高版本


<div id="indoorSearch"></div>

# **indoorSearch**

搜索室内地图内容

indoorSearch({params}, callback(ret, err))

## params

strID：

- 类型：字符串
- 描述：室内ID

keyword：

- 类型：字符串
- 描述：关键字

pageIndex：

- 类型：数字
- 描述：分页索引，可选，默认为0

pageCapacity：

- 类型：数字
- 描述：分页数量，可选，默认为10，最多为50

## callback(ret，err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
      status: true,         //布尔型；是否检索成功true||false
      totalPoiNum: ,        //数字类型；本次POI室内搜索的总结果数
      currPoiNum: ,         //数字类型；当前页的室内POI结果数(android不支持)
      pageNum: ,            //数字类型；本次POI室内搜索的总页数
      pageIndex: ,          //数字类型；当前页的索引(android不支持)
      poiIndoorInfoList: [{ //数组类型；返回搜索结果列表
        name: '',           //字符串类型；名称
        uid: '',            //字符串类型；POIuid
        indoorId: '',       //字符串类型；该室内POI所在 室内ID
        floor: '',          //字符串类型；该室内POI所在楼层
        address: '',        //字符串类型；地址
        city: '',           //字符串类型；所在城市(android不支持)
        cid: '',           //字符串类型；所在城市id(ios不支持)
        phone: '',          //字符串类型；电话号码
        pt: {               //JOSN对象；位置信息
           latitude:,       //数字类型；维度
           longtitude:      //数字类型；经度
        },                   
        tag: '',            //字符串类型；POI标签
        price: '',          //数字类型；价格
        starLevel: '',      //数字类型；星级（0-50），50表示五星
        grouponFlag: '',    //数字类型；是否有团购
        takeoutFlag: 0 ,    //数字类型；是否有外卖
        waitedFlag: '',     //数字类型；是否排队
        grouponNum: '',     //数字类型；团购数,-1表示没有团购信息
      }] 
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（该城市不支持公交搜索）
                      //4（不支持跨城市公交）
                      //5（没有找到检索结果）
                      //6（起终点太近）
                      //7（key错误）
                      //8（网络连接错误）
                      //9（网络连接超时）
                      //10（还未完成鉴权，请在鉴权通过后重试）
                      //11（室内图ID错误）
                      //12（室内图检索楼层错误）
                      //13（起终点不在支持室内路线的室内图内）
                      //14（起终点不在同一个室内）
                      //15（参数错误）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.indoorSearch({
    strID: '',
    keyword: ''
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    } else {
        alert(JSON.stringify(err));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本


<div id="addAnnotations"></div>

# **addAnnotations**

在地图上添加标注信息

addAnnotations({params}, callback(ret))

## params

annotations：

- 类型：数组
- 描述：图标标注信息组成的数组
- 内部字段：

```js
[{
    id: 1,                     //数字类型；图标标注的唯一标识
    lon: 116.233,              //数字类型；图标标注所在位置的经度
    lat: 39.134,               //数字类型；图标标注所在位置的纬度
    icon: 'widget://',         //（可选项）字符串类型；指定的标注图标，要求本地路径（fs://、widget://），若不传则显示公用的 icon 图标
    draggable: true            //（可选项）布尔类型；所添加的标注是否可被拖动，若不传则以公用的 draggable 为准
}]
```

icon：

- 类型：字符串
- 描述：（可选项）公用的标注图标，要求本地路径（fs://、widget://）
- 默认值：红色大头针


draggable：

- 类型：布尔
- 描述：（可选项）所添加的标注是否可被拖动
- 默认值：false

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    id: 10      //数字类型；相应事件的标注的
    eventType: 'clickContent',   //字符串类型；交互事件类型
                                //取值范围：
                                //click（用户点击标注事件）
                                //drag（用户拖动标注事件）
    dragState: 'starting'       //字符串类型；标注被拖动的状态，当 eventType 为 drag 时本字段有值，
                                //取值范围：
                                //starting（开始拖动）
                                //dragging （拖动中）
                                //ending （拖动结束）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.addAnnotations({
    annotations: [{
        id: 1,
        lon: 116.297,
        lat: 40.109
    }, {
        id: 2,
        lon: 116.29,
        lat: 40.109
    }, {
        id: 3,
        lon: 116.298,
        lat: 40.11
    }],
    icon: 'widget://',
    draggable: true
}, function(ret) {
    if (ret) {
        alert(ret.id);
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getAnnotationCoords"></div>

# **getAnnotationCoords**

获取指定标注的经纬度

getAnnotationCoords({params}, callback(ret))

## params

id：

- 类型：数字
- 描述：指定的标注 id

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    lon: 116.213,      //数字类型；标注的经度
    lat: 39.213        //数字类型；标注的纬度
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getAnnotationCoords({
    id: 2
}, function(ret) {
    if (ret) {
        api.alert({ msg: JSON.stringify(ret) });
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setAnnotationCoords"></div>

# **setAnnotationCoords**

设置某个已添加标注的经纬度

setAnnotationCoords(callback(ret, err))

## params

id：

- 类型：数字
- 描述：指定的标注 id

lon：

- 类型：数字
- 描述：设置的经度

lat：

- 类型：数字
- 描述：设置的纬度

## 示例代码

```js
var map = api.require('bMap');
map.setAnnotationCoords({
    id: 2,
    lon: 116.39,
    lat: 40.209
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="annotationExist"></div>

# **annotationExist**

判断标注是否存在

annotationExist({params}, callback(ret))

## params

id：

- 类型：数字
- 描述：指定的标注 id

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true      //布尔类型；标注是否存在，true || false
}
```

## 示例代码

```js
var map = api.require('bMap');
map.annotationExist({
    id: 2
}, function(ret) {
    if (ret.status) {
        api.alert({ msg: '存在' });
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="setBubble"></div>

# **setBubble**

设置点击标注时弹出的气泡信息

setBubble({params}, callback(ret))

## params

id：

- 类型：数字
- 描述：要设置气泡的标注 id

bgImg：

- 类型：字符串
- 描述：（可选项）弹出气泡的背景图片（160*90规格），要求本地路径（fs://、widget://）
- 默认值：默认气泡样式

content：

- 类型：JSON 对象
- 描述：弹出气泡的内容
- 内部字段：

```js
{
    title: '',             //字符串类型；弹出气泡的标题
    subTitle: '',          //（可选项）字符串类型；弹出气泡的概述内容，若不传则 title 在上下位置居中显示
    illus: ''              //（可选项）字符串类型；弹出气泡的配图（30*40规格），支持http://、https://、widget://、fs://等协议
}
```

styles：

- 类型：JSON 对象
- 描述：弹出气泡的样式
- 内部字段：

```js
{
    titleColor: '#000',             //（可选项）字符串类型；气泡标题的文字颜色，支持rgb、rgba、#；默认：'#000'
    titleSize: 16,                  //（可选项）数字类型；气泡标题的文字大小；默认：16
    subTitleColor: '#000',          //（可选项）字符串类型；气泡概述内容的文字颜色，支持rgb、rgba、#；默认：'#000'
    subTitleSize: 14,               //（可选项）数字类型；气泡概述内容的文字大小；默认：14
    illusAlign: 'left'              //（可选项）字符串类型；气泡配图的显示位置；默认：'left'
                                    //取值范围：
                                    //left（图片居左）
                                    //right（图片居右）
}
```

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    id: 10,                     //数字类型；用户点击气泡返回的id
    eventType: 'clickContent',   //字符串类型；交互事件类型
                                //取值范围：
                                //clickContent（点击气泡文本内容）
                                //clickIllus（点击配图）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.setBubble({
    id: 2,
    bgImg: 'widget://res/bubble_bg.png',
    content: {
        title: '大标题',
        subTitle: '概述内容',
        illus: 'http://ico.ooopic.com/ajax/iconpng/?id=145044.png'
    },
    styles: {
        titleColor: '#000',
        titleSize: 16,
        subTitleColor: '#999',
        subTitleSize: 12,
        illusAlign: 'left'
    }
}, function(ret) {
    if (ret) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="popupBubble"></div>

# **popupBubble**

弹出指定标注的气泡

popupBubble({params})

## params

id：

- 类型：数字
- 描述：气泡的 id

## 示例代码

```js
var map = api.require('bMap');
map.popupBubble({
    id: 2
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="closeBubble"></div>

# **closeBubble**

关闭已弹出的气泡

closeBubble({params})

## params

id：

- 类型：数字
- 描述：气泡的 id

## 示例代码

```js
var map = api.require('bMap');
map.closeBubble({
    id: 2
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addBillboard"></div>

# **addBillboard**

在地图上添加布告牌

addBillboard({params})

## params

id：

- 类型：数字
- 描述：布告牌的 id，**注意：本 id 不可与 addAnnotations 接口内的 id 相同**

coords：

- 类型：JSON 对象
- 描述：布告牌所在位置的坐标
- 内部字段：

```js
{
    lon: 116.233,            //数字类型；布告牌所在位置的经度
    lat: 39.134              //数字类型；布告牌所在位置的纬度
}
```

bgImg：

- 类型：字符串
- 描述：布告牌的背景图片（160*75规格），要求本地路径（fs://、widget://）

content：

- 类型：JSON 对象
- 描述：布告牌的内容
- 内部字段：

```js
{
    title: '',             //（可选项）字符串类型；布告牌的标题
    subTitle: '',          //（可选项）字符串类型；布告牌的概述内容 
    illus: ''              //（可选项）字符串类型；布告牌的配图（35*50规格），支持http://、https://、widget://、fs://等协议
}
```

styles：

- 类型：JSON 对象
- 描述：布告牌的样式
- 内部字段：

```js
{
    titleColor: '#000',             //（可选项）字符串类型；布告牌标题的文字颜色，支持rgb、rgba、#；默认：'#000'
    titleSize: 14,                  //（可选项）数字类型；布告牌标题的文字大小；默认：16
    subTitleColor: '#000',          //（可选项）字符串类型；布告牌概述内容的文字颜色，支持rgb、rgba、#；默认：'#000'
    subTitleSize: 12,               //（可选项）数字类型；布告牌概述内容的文字大小；默认：16
    illusAlign: 'left'              //（可选项）字符串类型；布告牌配图的显示位置；默认：'left'
                                    //取值范围：
                                    //left（图片居左）
                                    //right（图片居右）
}
```

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    id: 4,                     //数字类型；用户点击布告牌返回的id
}
```

## 示例代码

```js
var map = api.require('bMap');
map.addBillboard({
    id: 4,
    coords: {
        lon: 116.233,
        lat: 39.134
    },
    bgImg: 'widget://image/bMapTest.png',
    content: {
        title: '大标题大标题大标题大标题',
        subTitle: '概述内容概述内容概述内容',
        illus: 'http://ico.ooopic.com/ajax/iconpng/?id=145044.png'
    },
    styles: {
        titleColor: '#000',
        titleSize: 16,
        subTitleColor: '#999',
        subTitleSize: 12,
        illusAlign: 'left'
    }
}, function(ret) {
    if (ret) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addMobileAnnotations"></div>

# **addMobileAnnotations**

在地图上添加可移动、旋转的标注图标，**注意：本 id 不可与 addAnnotations、addBillboard 接口内的 id 相同**

addMobileAnnotations({params})

## params

annotations：

- 类型：数组
- 描述：图标标注信息组成的数组
- 内部字段：

```js
[{
    id: 10,                    //数字类型；图标标注的唯一标识
    lon: 116.233,              //数字类型；图标标注所在位置的经度
    lat: 39.134,               //数字类型；图标标注所在位置的纬度
    icon: 'widget://'          //字符串类型；指定的标注图标，要求本地路径（fs://、widget://）
}]
```

## 示例代码

```js
var map = api.require('bMap');
map.addMobileAnnotations({
    annotations: [{
        id: 10,
        lon: 116.297,
        lat: 40.109,
        icon: 'widget://image/bMap_car1.png'
    }, {
        id: 11,
        lon: 116.98,
        lat: 40.109,
        icon: 'widget://image/bMap_car2.png'
    }, {
        id: 12,
        lon: 115.30,
        lat: 40.109,
        icon: 'widget://image/bMap_car3.png'
    }, {
        id: 13,
        lon: 116.297,
        lat: 39.109,
        icon: 'widget://image/bMap_car1.png'
    }, {
        id: 14,
        lon: 116.98,
        lat: 39.109,
        icon: 'widget://image/bMap_car2.png'
    }, {
        id: 15,
        lon: 115.30,
        lat: 39.109,
        icon: 'widget://image/bMap_car3.png'
    }]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="moveAnnotation"></div>

# **moveAnnotation**

移动地图上已添加的可移动、旋转的标注图标，**在移动动画开始前，会先做 0.3 秒的旋转动画，使所移动的图标中间轴线顶端对准终点坐标点。由于百度官方 SDK 的 bug 限制，在 Android 平台上，如果标注添加到地图当前可视区域以外的区域，则不可以移动该标注**

moveAnnotation({params}, callback(ret, err))

## params

id：

- 类型：数字
- 描述：要移动的标注的 id 

duration：

- 类型：数字
- 描述：（可选项）标注图标移动动画的时间，单位为秒（s），**不包括旋转动画时间**
- 默认值：1.0（s）

end：

- 类型：JSON 对象
- 描述：终点经纬度
- 内部字段：

```js
{
    lon: 116.581515,    //数字类型；终点的经度
    lat: 29.615467      //数字类型；终点的纬度
}
```
## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    id:         //数字类型；移动动画结束的标注的 id
}
```

## 示例代码

```js
var map = api.require('bMap');
for (var i = 0; i < 6; i++) {
    map.moveAnnotation({
        id: 10 + i,
        duration: 6,
        end: {
            lon: 116.3843839609304,
            lat: 39.98964439091298
        }
    }, function(ret, err) {
        alert(ret.id + '移动结束')
    });
}
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="removeAnnotations"></div>

# **removeAnnotations**

移除指定 id 的标注（可移动、不可移动）或布告牌

removeAnnotations({params})

## params

ids：

- 类型：数组
- 描述：（可选项）要移除的标注或布告牌id（数字），若不传或传空则移除所有addAnnotations、addBillboard、addMobileAnnotations接口添加的标注

## 示例代码

```js
var map = api.require('bMap');
map.removeAnnotations({
    ids: [1,3,5,7]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addLine"></div>

# **addLine**

在地图上添加折线

addLine({params})

## params

id：

- 类型：数字
- 描述：折线的 id

styles：

- 类型：JSON 对象
- 描述：（可选项）折线的样式
- 内部字段：

```js
{
    borderColor: '#000',                //（可选项）字符串类型；折线的颜色，支持rgb、rgba、#；默认值：'#000'
    borderWidth: 3                      //（可选项）数字类型；折线的宽度，默认：1
}
```

points：

- 类型：数组
- 描述：折线的多个点组成的数组
- 内部字段：

```js
[{
    lon: 116.297,     //数字类型；经度
    lat: 40.109       //数字类型；纬度
}]
```

## 示例代码

```js
var map = api.require('bMap');
map.addLine({
    id: 1,
    styles: {
        borderColor: '#FF0000',
        borderWidth: 3
	 },
	 points: [{
	     lon:116.39432327,
	     lat:39.98963192
	 },{
	     lon: 116.49432328,
	     lat: 39.98963192
	 },{
	     lon: 116.39432327,
	     lat: 39.88933191
	 }]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addPolygon"></div>

# **addPolygon**

在地图上添加多边形

addPolygon({params})

## params

id：

- 类型：数字
- 描述：多边形的 id，**不可与 addLine 接口内的 id 相同**

styles：

- 类型：JSON 对象
- 描述：（可选项）多边形的样式
- 内部字段：

```js
{
    borderColor: '#000',    //（可选项）字符串类型；多边形的边框颜色，支持rgb、rgba、#；默认：'#000'
    fillColor: '#000',      //（可选项）字符串类型；多边形的填充色，支持rgb、rgba、#；默认：'#000'
    borderWidth: 3          //（可选项）数字类型；多边形的边框宽度，默认：1
}
```

points：

- 类型：数组
- 描述：多边形的各个点组成的数组
- 内部字段：

```js
[{
    lon: 116.297,      //数字类型；经度
    lat: 40.109        //数字类型；纬度
}]
```

## 示例代码

```js
var map = api.require('bMap');
map.addPolygon({
    id: 2,
    styles: {
        borderColor: '#FF0000',
        borderWidth: 3,
        fillColor: '#0000ff'
    },
    points: [{
        lon: 116.39432327,
        lat: 39.98963192
    }, {
        lon: 116.49432328,
        lat: 39.98963192
    }, {
        lon: 116.39432327,
        lat: 39.88933191
    }]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addArc"></div>

# **addArc**

在地图上添加弧形

addArc({params})

## params

id：

- 类型：数字
- 描述：多边形的 id，**不可与 addLine、addPolygon 接口内的 id 相同**

styles：

- 类型：JSON 对象
- 描述：（可选项）弧形的样式
- 内部字段：

```js
{
    borderColor: '#000',    //（可选项）字符串类型；弧形的边框颜色，支持rgb、rgba、#；默认：'#000'
    borderWidth: 3          //（可选项）数字类型；弧形的边框宽度，默认：1
}
```

points：

- 类型：数组
- 描述：弧形的各个点（弧形两端点和弧形中间点）组成的数组
- 内部字段：

```js
[{
    lon: 116.297,      //数字类型；经度
    lat: 40.109        //数字类型；纬度
}]
```

## 示例代码

```js
var map = api.require('bMap');
map.addArc({
    id: 3,
    styles: {
        borderColor: '#FF0000',
        borderWidth: 3
    },
    points: [{
        lon: 116.39432327,
        lat: 39.98963192
    }, {
        lon: 116.49432328,
        lat: 39.98963192
    }, {
        lon: 116.39432327,
        lat: 39.88933191
    }]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addCircle"></div>

# **addCircle**

在地图上添加圆形

addCircle({params})

## params

id：

- 类型：数字
- 描述：圆形的 id,**不可与 addLine、addPolygon、addArc 接口的 id 相同**

center：

- 类型：JSON 对象
- 描述：圆形中心点的经纬度
- 内部字段：

```js
{
    lon: 116.297,       //数字类型；圆形中心点的经度
    lat: 40.109         //数字类型；圆形中心点的纬度
}
```

radius：

- 类型：数字
- 描述：圆形的半径

styles：

- 类型：JSON 对象
- 描述：（可选项）圆形的样式
- 内部字段：

```js
{
    borderColor: '#000',    //（可选项）字符串类型；圆形的边框颜色，支持rgb、rgba、#；默认：'#000'
    fillColor: '#000',      //（可选项）字符串类型；圆形的填充色，支持rgb、rgba、#；默认：'#000'
    borderWidth: 3          //（可选项）数字类型；圆形的边框宽度，默认：1
}
```

## 示例代码

```js
var map = api.require('bMap');
map.addCircle({
    id: 4,
    center: {
        lon: 116.39432327,
        lat: 39.98963192
    },
    radius: 500,
    styles: {
        borderColor: '#FF0000',
        borderWidth: 3,
        fillColor: '#0000ff'
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addImg"></div>

# **addImg**

在地图上添加图片

addImg({params})

# **params**

id：

- 类型：数字
- 描述：图片 id，**不可与 addLine、addPolygon、addArc、addCircle 接口的 id 相同**

imgPath：

- 类型：字符串
- 描述：图片的路径，要求本地路径（fs://、widget://）

lbLon：

- 类型：数字
- 描述：左下角点的经度

lbLat：

- 类型：数字
- 描述：左下角点的纬度

rtLon：

- 类型：数字
- 描述：右上角点的经度

rtLat：

- 类型：数字
- 描述：右上角点的纬度

opacity：

- 类型：数字
- 描述：图片透明度，取值范围：0-1
- 默认：1

## 示例代码

```js
var map = api.require('bMap');
map.addImg({
    id: 5,
    imgPath: 'widget://res/over_img.png',
    lbLon: 116.39432327,
    lbLat: 39.88933191,
    rtLon: 116.49432328,
    rtLat: 39.98963192,
    opacity: 0.8
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="removeOverlay"></div>

# **removeOverlay**

移除指定 id 的覆盖物（addLine、addPolygon、addArc、addCircle、addImg添加的覆盖物）

removeOverlay({params})

## params

ids：

- 类型：数组
- 描述：要移除的 id（数字）组成的数组

## 示例代码

```js
var map = api.require('bMap');
map.removeOverlay({
    ids: [1, 2, 3, 4, 5]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="searchRoute"></div>

# **searchRoute**

搜索路线方案，**无需调用 open 接口即可使用**

searchRoute({params}, callback(ret, err))

## params

id：

- 类型：数字
- 描述：搜索的路线 id ，drawRoute 时使用

type：

- 类型：字符串
- 描述：（可选项）路线类型
- 默认值：transit
- 取值范围：
    - drive（开车）
    - transit（公交）
    - walk（步行）

policy：

- 类型：字符串
- 描述：（可选项）路线策略，**type 为 walk（步行）时，此参数可不传**
- 默认值：'ebus_time_first/ecar_time_first'
- 取值范围：
    - ecar_fee_first（驾乘检索策略常量：较少费用）
    - ecar_dis_first（驾乘检索策略常量：最短距离）
    - ecar_time_first（驾乘检索策略常量：时间优先）
    - ecar_avoid_jam（驾乘检索策略常量：躲避拥堵）
    - ebus_no_subway（公交检索策略常量：不含地铁）
    - ebus_time_first（公交检索策略常量：时间优先）
    - ebus_transfer_first（公交检索策略常量：最少换乘）
    - ebus_walk_first（公交检索策略常量：最少步行距离）

start：

- 类型：JSON 对象
- 描述：起点信息
- 内部字段：

```js
{
    lon: 116.403838,    //（可选项）数字类型；起点经度
    lat: 39.914437      //（可选项）数字类型；起点纬度
}
```

end：

- 类型：JSON 对象
- 描述：终点信息
- 内部字段：

```js
{
    lon: 116.384852,    //（可选项）数字类型；终点经度
    lat: 39.989576      //（可选项）数字类型；终点纬度
}
```

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,                   //布尔型；true||false
    plans: [{                       //数组类型；路线方案描述
        start:{                     //JSON对象；起点信息
           lon: 116.384852,         //数字类型；起点经度
           lat: 39.989576,          //数字类型；起点纬度
           description: ''          //字符串类型；起点说明文字
        },
        end:{                       //JSON对象；终点信息
           lon: 116.384852,         //数字类型；终点经度
           lat: 39.989576,          //数字类型；终点纬度
           description: ''          //字符串类型；终点说明文字
        },
        nodes:[{                    //数组类型；路线经过的结点信息组成的数组
            lon: 116.384852,        //数字类型；结点经度
            lat: 39.989576,         //数字类型；结点纬度
            degree: 30,             //数字类型；结点转弯角度
            description: ''         //字符串类型；结点说明文字
        }],
        distance: 1000,             //数字类型；路线长度，单位：米
        duration: 10                //数字类型；路线耗时，单位：秒
    }]
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1,            //数字类型；
                        //错误码：
                        //-1（未知错误）
                        //1（检索词有歧义）
                        //2（检索地址有歧义）
                        //3（该城市不支持公交搜索）
                        //4（不支持跨城市公交）
                        //5（没有找到检索结果）
                        //6（起终点太近）
                        //7（key错误）
                        //8（网络连接错误）
                        //9（网络连接超时）
                        //10（还未完成鉴权，请在鉴权通过后重试）
    //数组类型；建议起点信息，若传入的起点信息不明确，则返回该字段
    suggestStarts: [{   
        name: '',       //字符串类型；地点名
        city: '',       //字符串类型；地点所在城市
        lon: 116.213,   //数字类型；地点经度
        lat: 39.213     //数字类型；地点纬度
    }],
    //数组类型；建议终点信息，若传入的终点信息不明确，则返回该字段
    suggestEnds: [{     
        name: '',       //字符串类型；地点名
        city: '',       //字符串类型；地点所在城市
        lon: 116.213,   //数字类型；地点经度  
        lat: 39.213     //数字类型；地点纬度
    }]
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchRoute({
    id: 1,
    type: 'drive',
    policy: 'ecar_fee_first',
    start: {
        lon: 116.403838,
        lat: 39.914437
    },
    end: {
        lon: 116.384852,
        lat: 39.989576
    }
}, function(ret, err) {
    if (ret.status) {
        api.alert({ msg: JSON.stringify(ret) });
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="drawRoute"></div>

# **drawRoute**

在地图上显示指定路线，**调用本接口前，必须保证已经调用过接口 open 和 searchRoute**

drawRoute({params}, callback(ret, err))

## params

id：

- 类型：数字
- 描述：路线 id （searchRoute 时传的 id），removeRoute 时使用此 id 移除路线

autoresizing：

- 类型：布尔
- 描述：路线渲染结束是否自动调整地图可视区域
- 默认值：true

index：

- 类型：数字类型
- 描述：路线方案的索引，在 searchRoute 时返回的多个路线方案组成的数组中的索引
- 默认值：0

styles：

- 类型：JSON 对象
- 描述：路线样式设置
- 内部字段：

```js
{
    start: {         
       icon: ''      //（可选项）字符串类型；起点图标，有默认图标
    },
    end: {           
       icon: ''      //（可选项）字符串类型；终点图标，有默认图标
    }
}
```

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    nodeIndex: 0,    //数字类型；点击路线上结点的索引（该路线在所属方案 nodes 数组内的索引）
    routeId: 1       //数字类型；点击的结点所在的路线的 id
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchRoute({
    type: 'drive',
    policy: 'ecar_fee_first',
    start: {
        lon: 116.403838,
        lat: 39.914437
    },
    end: {
        lon: 116.384852,
        lat: 39.989576
    }
}, function(ret, err) {
    if (ret.status) {
        map.drawRoute({
            id: 1,
            autoresizing: false,
            index: 0,
            styles: {
                start: {
                    icon: 'widget://image/bmap_start.png'
                },
                end: {
                    icon: 'widget://image/bmap_end.png'
                }
            }
        }, function(ret) {
            api.alert({ msg: JSON.stringify(ret) });
        });
    } else {
        api.alert({ msg: JSON.stringify(err) });
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="removeRoute"></div>

# **removeRoute**

移除指定 id 的路线

removeRoute({params})

## params

ids：

- 类型：数组
- 描述：所要移除的 id（数字）组成的数组

## 示例代码

```js
var map = api.require('bMap');
map.removeRoute({
    ids: [1, 2, 3]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="searchBusRoute"></div>

# **searchBusRoute**

根据关键字搜索公交、地铁线路，**无需调用 open 接口即可搜索**

searchBusRoute({params}, callback(ret, err))

## params

city：

- 类型：字符串
- 描述：城市

line：

- 类型：字符串
- 描述：公交、地铁线路号（例如：1路，1号线）

## callback(ret，err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
      status: true,     //布尔型；true||false
      results: [{           //数组类型；返回搜索结果列表
        name: '',           //字符串类型；名称
        uid: '',            //字符串类型；兴趣点 uid
        city: '',           //字符串类型；所在城市
        poiType: 0          //数字类型；POI 类型
                            //取值类型：
                            //2（公交线路）
                            //4（地铁线路）
      }] 
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchBusRoute({
    city: '北京',
    line: '110'
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    } else {
        alert(JSON.stringify(err));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="drawBusRoute"></div>

# **drawBusRoute**

根据 searchBusRoute 搜索返回的 uid 查询线路详情并绘制在地图上

drawBusRoute({params}, callback(ret, err))

## params

id：

- 类型：数字
- 描述：地图上显示的公交、地铁路线的 id，**removeBusRoute 时使用此 id**

autoresizing：

- 类型：布尔
- 描述：路线渲染结束是否自动调整地图可视区域
- 默认值：true

city：

- 类型：字符串
- 描述：城市

uid：

- 类型：字符串
- 描述：searchBusRoute 接口获取到的目标兴趣点的 uid

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,              //布尔型；true||false
    eventType: 'draw',         //字符串类型；回调事件类型
                               //取值范围：
                               //draw（添加路线）
                               //click（用户点击路线上的结点）
    name: '',                  //字符串类型；公交线路名称
    company: '',               //字符串类型；公交公司名称
    startTime: '',             //字符串类型；公交路线首班车时间，格式为：hh:ss
    endTime: '',               //字符串类型；公交路线末班车时间，格式为：hh:ss
    isMonTicket: '',           //布尔类型；公交是线是否有月票
    stations: [{               //数组类型；所有站点信息
       lon: 116.404,           //数字类型；公交站经度
       lat: 39.915,            //数字类型；公交站纬度
       description: ''         //字符串类型；公交站描述
    }],
    nodeIndex: 0,              //数字类型；点击路线上结点的索引（在 stations 数组内的索引）
    routeId: 1                 //数字类型；点击的结点所在的路线的 id
}
```
err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchBusRoute({
    city: '北京',
    line: '110'
},function(ret, err){
    if(ret.status){
       var results = ret.results;
       for(var i=0, len=results.length; i<len; i++){
            var res = results[i];
            map.drawBusRoute({
                id: i+1,
                autoresizing:false,
                city: res.city,
                uid: res.uid,
                nodeShow: false
            },function(ret){
                if(ret.status){
                    alert(JSON.stringify(ret));
                }else{
                    alert(JSON.stringify(err));
                }
            });     
       }
    }else{
        alert(JSON.stringify(err));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="removeBusRoute"></div>

# **removeBusRoute**

移除地图上显示的公交、地铁线路

removeBusRoute({params})

## params

ids：

- 类型：数组
- 描述：所要移除的公交、地铁线路的 id（数字）组成的数组

## 示例代码

```js
var map = api.require('bMap');
map.removeBusRoute({
   ids:[1]
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="searchInCity"></div>

# **searchInCity**

根据单个关键字搜索兴趣点，**无需调用 open 接口即可搜索**

searchInCity({params}, callback(ret, err))

## params

city：

- 类型：字符串
- 描述：要搜索的城市

keyword：

- 类型：字符串
- 描述：搜索的关键字

pageIndex：

- 类型：数字
- 描述：（可选项）分页索引
- 默认：0

pageCapacity：

- 类型：数字
- 描述：（可选项）每页包含数据条数，最多为50
- 默认：10

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    totalNum: 10,           //数字类型；本次搜索的总结果数
    currentNum: 5,          //数字类型；当前页的结果数
    totalPage: 10,          //数字类型；本次搜索的总页数
    pageIndex: 1,           //数字类型；当前页的索引
    results: [{             //数组类型；返回搜索结果列表
        lon: 116.213,       //数字类型；当前内容的经度
        lat: 39.213,        //数字类型；当前内容的纬度
        name: '',           //字符串类型；名称
        uid: 123            //数字类型；兴趣点的id
        address: '',        //字符串类型；地址
        city: '',           //字符串类型；所在城市
        phone: '',          //字符串类型；电话号码
        poiType: 0          //数字类型；POI 类型
                            //取值类型：
                            //0（普通点）
                            //1（公交站）
                            //2（公交线路）
                            //3（地铁站）
                            //4（地铁线路）
    }]             
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchInCity({
    city: '北京',
    keyword: '学校',
    pageIndex: 0,
    pageCapacity: 20
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    } else {
        alert(JSON.stringify(err));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="searchNearby"></div>

# **searchNearby**

根据单个关键字在圆形区域内搜索兴趣点，**无需调用 open 接口即可搜索**

searchNearby({params}, callback(ret, err))

## params

keyword：

- 类型：字符串
- 描述：搜索关键字

lon：

- 类型：数字
- 描述：指定区域中心点的经度

lat：

- 类型：数字
- 描述：指定区域中心点的纬度

radius：

- 类型：数字
- 描述：指定区域的半径，单位为 m（米）

pageIndex：

- 类型：数字
- 描述：（可选项）分页索引
- 默认：0

pageCapacity：

- 类型：数字
- 描述：（可选项）每页包含数据条数，最多为50
- 默认：10

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    totalNum: 20,           //数字类型；本次搜索的总结果数
    currentNum: 5,          //数字类型；当前页的结果数
    totalPage: 3,           //数字类型；本次搜索的总页数
    pageIndex: 1,           //数字类型；当前页的索引
    results: [{             //数组类型；返回搜索结果列表
        lon: 116.213,       //数字类型；当前内容的经度
        lat: 39.213,        //数字类型；当前内容的纬度
        name: '',           //字符串类型；名称
        uid: 123            //数字类型；兴趣点id
        address: '',        //字符串类型；地址
        city: '',           //字符串类型；所在城市
        phone: '',          //字符串类型；电话号码
        poiType: 0          //数字类型；POI 类型
                            //取值类型：
                            //0（普通点）
                            //1（公交站）
                            //2（公交线路）
                            //3（地铁站）
                            //4（地铁线路）
    }]  
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchNearby({
    keyword: 'KTV',
    lon: 116.384767,
    lat: 39.989539,
    radius: 2000
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    } else {
        alert(JSON.stringify(err));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="searchInBounds"></div>

# **searchInBounds**

根据单个关键字在方形区域内搜索兴趣点，**无需调用 open 接口即可搜索**

searchInBounds({params}, callback(ret, err))

## params

keyword：

- 类型：字符串
- 描述：搜索关键字

lbLon：

- 类型：数字
- 描述：矩形左下角的经度

lbLat：

- 类型：数字
- 描述：矩形左下角的纬度

rtLon：

- 类型：数字
- 描述：矩形右上角的经度

rtLat：

- 类型：数字
- 描述：矩形右上角的纬度

pageIndex：

- 类型：数字
- 描述：（可选项）分页索引
- 默认：0

pageCapacity：

- 类型：数字
- 描述：（可选项）每页包含数据条数，最多为50
- 默认：10

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    totalNum: 20,           //数字类型；本次搜索的总结果数
    currentNum: 5,          //数字类型；当前页的结果数
    totalPage: 3,           //数字类型；本次搜索的总页数
    pageIndex: 1,           //数字类型；当前页的索引
    results: [{             //数组类型；返回搜索结果列表
        lon: 116.213,       //数字类型；当前内容的经度
        lat: 39.213,        //数字类型；当前内容的纬度
        name: '',           //字符串类型；名称
        uid: 123            //数字类型；兴趣点id
        address: '',        //字符串类型；地址
        city: '',           //字符串类型；所在城市
        phone: '',          //字符串类型；电话号码
        poiType: 0          //数字类型；POI 类型
                            //取值类型：
                            //0（普通点）
                            //1（公交站）
                            //2（公交线路）
                            //3（地铁站）
                            //4（地铁线路）
    }]
}
```

err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchInBounds({
    keyword: '图书馆',
    lbLon: 112.47723797622677,
    lbLat: 34.556480000000015,
    rtLon: 109.77539000000002,
    rtLat: 33.43144
}, function(ret, err) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    } else {
        alert(JSON.stringify(err));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本


<div id="autocomplete"></div>

# **autocomplete**

根据关键字返回建议搜索关键字，**无需调用 open 接口即可搜索**

autocomplete({params}, callback(ret, err))

## params

keyword：

- 类型：字符串
- 描述：关键字

city：

- 类型：字符串
- 描述：（可选项）要搜索的城市

## callback(ret, err)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    results: []             //数组类型；返回建议搜索关键字组成的数组          
}
```
err：

- 类型：JSON 对象
- 内部字段：

```js
{
    code: 1           //数字类型；错误码
                      //1（检索词有岐义）
                      //2（检索地址有岐义）
                      //3（没有找到检索结果）
                      //4（key错误）
                      //5（网络连接错误）
                      //6（网络连接超时）
                      //7（还未完成鉴权，请在鉴权通过后重试）
}
```

## 示例代码

```js
var map = api.require('bMap');
map.autocomplete({
    keyword: '北京西站',
    city: '北京'
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret.results));
    } else {
        alert(JSON.stringify(err));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getHotCityList"></div>

# **getHotCityList**

获取热门城市列表，**无需调用 open 接口**

getHotCityList(callback(ret))


## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    records : [{            //数组类型；返回热门城市的信息 
        name: '',           //字符串类型；城市名称
        size:  ,            //数字类型；数据包总大小
        cityID: ,           //数字类型；城市ID
        cityType: ,         //数字类型；城市类型，0：全国；1：省份；2：城市；如果是省份，可以通过childCities得到子城市列表
        childCities:[{     //数组类型；子城市列表
           name: '',       //字符串类型；城市名称
           cityID:         //数字类型；城市ID
        }]      
    }]                      
}
```

##示例代码

```js
var map = api.require('bMap');
map.getHotCityList(function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getOfflineCityList"></div>

# **getOfflineCityList**

获取支持离线下载城市列表，**无需调用 open 接口**

getOfflineCityList(callback(ret))


## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    records : [{            //数组类型；返回支持离线下载城市的信息 
        name: '',           //字符串类型；城市名称
        size:  ,            //数字类型；数据包总大小
        cityID: ,           //数字类型；城市ID
        cityType: ,         //数字类型；城市类型，0：全国；1：省份；2：城市；如果是省份，可以通过childCities得到子城市列表
        childCities:[{     //数组类型；子城市列表
           name: '',       //字符串类型；城市名称
           cityID:         //数字类型；城市ID
        }]    
    }]                      
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getOfflineCityList(function(ret){
    if(ret.status){
        alert(JSON.stringify(ret)); 
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="searchCityByName"></div>

# **searchCityByName**

根据城市名搜索该城市离线地图记录，**无需调用 open 接口**

searchCityByName({params}, callback(ret))

## params

name：

- 类型：字符串
- 描述：指定搜索的城市名

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    records : [{            //数组类型；返回支持离线下载城市的信息 
        name: '',           //字符串类型；城市名称
        size:  ,            //数字类型；数据包总大小
        cityID: ,           //数字类型；城市ID
        cityType: ,         //数字类型；城市类型，0：全国；1：省份；2：城市；如果是省份，可以通过childCities得到子城市列表
        childCities:[{     //数组类型；子城市列表
           name: '',       //字符串类型；城市名称
           cityID:         //数字类型；城市ID
        }]    
    }]                      
}
```

## 示例代码

```js
var map = api.require('bMap');
map.searchCityByName({
    name: "北京"
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getAllUpdateInfo"></div>

# **getAllUpdateInfo**

获取各城市离线地图更新信息，**无需调用 open 接口**

getAllUpdateInfo(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    records : [{            //数组类型；返回支持离线下载城市的信息 
        name: '',           //字符串类型；城市名称
        size:  ,            //数字类型；数据包总大小
        cityID: ,           //数字类型；城市ID
        serversize: ,       //数字类型；服务端数据大小，当update为YES时有效，单位：字节
        ratio: ,            //数字类型；下载比率，100为下载完成，下载完成后会自动导入，status为4时离线包导入完成
        update: ,            //布尔类型；更新状态，离线包是否有更新（有更新需重新下载）
        lat: ,              //数字类型；城市中心点纬度坐标
        lon: ,              //数字类型；城市中心点纬度坐标
        status:             //数字类型；下载状态, 
                            //-1:未定义 
                            //1:正在下载　
                            //2:等待下载　
                            //3:已暂停　
                            //4:完成 
                            //5:校验失败 
                            //6:网络异常 
                            //7:读写异常 
                            //8:Wifi网络异常 
                            //9:离线包数据格式异常，需重新下载离线包 
                            //10:离线包导入中
    }]                      
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getAllUpdateInfo(function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="getUpdateInfoByID"></div>

# **getUpdateInfoByID**

获取指定城市id离线地图更新信息，**无需调用 open 接口**

getUpdateInfoByID({params}, callback(ret))

## params

cityID:

- 类型：数字
- 描述：指定的城市id

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false
    cityInfo : {            //JSON对象；返回指定城市的离线地图更新信息 
        name: '',           //字符串类型；城市名称
        size:  ,            //数字类型；数据包总大小
        cityID: ,           //数字类型；城市ID
        serversize: ,       //数字类型；服务端数据大小，当update为YES时有效，单位：字节
        ratio: ,            //数字类型；下载比率，100为下载完成，下载完成后会自动导入，status为4时离线包导入完成
        update: ,            //布尔类型；更新状态，离线包是否有更新（有更新需重新下载）
        lat: ,              //数字类型；城市中心点纬度坐标
        lon: ,              //数字类型；城市中心点纬度坐标
        status:             //数字类型；下载状态, 
                            //-1:未定义 
                            //1:正在下载　
                            //2:等待下载　
                            //3:已暂停　
                            //4:完成 
                            //5:校验失败 
                            //6:网络异常 
                            //7:读写异常 
                            //8:Wifi网络异常 
                            //9:离线包数据格式异常，需重新下载离线包 
                            //10:离线包导入中
    }                      
}
```

## 示例代码

```js
var map = api.require('bMap');
map.getUpdateInfoByID({
    cityID: 1
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="start"></div>

# **start**

启动下载指定城市 id 的离线地图，**无需调用 open 接口**

start({params}, callback(ret))

## params

cityID:

- 类型：数字
- 描述：指定的城市id

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false                  
}
```

## 示例代码

```js
var map = api.require('bMap');
map.start({
    cityID: 1
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="update"></div>

# **update**

启动更新指定城市 id 的离线地图，**无需调用 open 接口**

update({params}, callback(ret))

## params

cityID:

- 类型：数字
- 描述：指定的城市id

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false                  
}
```

## 示例代码

```js
var map = api.require('bMap');
map.update({
    cityID: 1
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="pause"></div>

# **pause**

暂停下载指定城市 id 的离线地图，**无需调用 open 接口**

pause({params}, callback(ret))

## params

cityID:

- 类型：数字
- 描述：指定的城市id

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false                  
}
```

## 示例代码

```js
var map = api.require('bMap');
map.pause({
    cityID: 1
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="remove"></div>

# **remove**

删除下载指定城市 id 的离线地图，**无需调用 open 接口**

remove({params}, callback(ret))

## params

cityID:

- 类型：数字
- 描述：指定的城市id

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    status: true,           //布尔型；true||false                  
}
```

## 示例代码

```js
var map = api.require('bMap');
map.remove({
    cityID: 1
}, function(ret) {
    if (ret.status) {
        alert(JSON.stringify(ret));
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="addOfflineListener"></div>

# **addOfflineListener**

监听离线地图相关事件

addOfflineListener(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    type: 0,                //数字类型；事件类型，取值范围如下：
                            //0：下载或更新
                            //1：检测到的压缩包个数
                            //2：当前解压的离线包
                            //3：错误的离线包
                            //4：有新版本
                            //5：扫描完毕
                            //6：新增离线包
    state:                  //数字类型；事件状态，
                            //当 type为 0 时，表示正在下载或更新城市id为state的离线包
                            //当 type 为1时，表示检测到state个离线压缩包
                            //当 type 为2时，表示正在解压第state个离线包
                            //当 type 为3时，表示有state个错误包
                            //当 type 为4时，表示id为state的城市离线包有更新
                            //当 type 为5时，表示扫瞄完成，成功导入state个离线包
                            //当 type 为6时，表示新安装的离线地图数目
}
```

## 示例代码

```js
var map = api.require('bMap');
map.addOfflineListener(function(ret) {
    switch (ret.type) {
        case 0:
            {
                map.getUpdateInfoByID({
                    cityID: 1
                }, function(ret) {
                    api.alert({ msg: ret.cityInfo.name + "下载进度：" + ret.cityInfo.ratio });
                });
            }
            break;
        case 1:
            {
                alert('检测到离线包个数是：' + ret.state);
            }
            break;
        case 2:
            {
                alert('正在解压第state个离线包，导入时会回调此类型');
            }
            break;
        case 3:
            {
                alert('有state个错误包，导入完成后会回调此类型');
            }
            break;
        case 4:
            {
                alert('id为state的state城市有新版本,可调用update接口进行更新');
            }
            break;
        case 5:
            {
                alert('导入成功state个离线包，导入成功后会回调此类型');
            }
            break;
        case 6:
            {
                alert('新增离线包');
            }
            break;
        default:
            break;
    }
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="removeOfflineListener"></div>

# **removeOfflineListener**

移除监听离线地图事件

removeOfflineListener()

## 示例代码

```js
var map = api.require('bMap');
map.removeOfflineListener();
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本
