/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import "UZBMap.h"
#import "UZAppUtils.h"
#import "NSDictionaryUtils.h"
#import "UZbMapAnnotation.h"
#import "BMapAsyncImageView.h"
#import "UZBMKRouteSearch.h"
#import "UZBMKPoiSearch.h"
#import "UZBMKBusLineSearch.h"
#import "UZBMKGeoCodeSearch.h"
#import "UZBMKPolyline.h"
#import <BaiduMapAPI_Location/BMKLocationComponent.h>
#import <BaiduMapAPI_Utils/BMKUtilsComponent.h>

#define MYBUNDLE_NAME @ "mapapi.bundle"
#define MYBUNDLE_PATH [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent: MYBUNDLE_NAME]
#define MYBUNDLE [NSBundle bundleWithPath: MYBUNDLE_PATH]
/*
typedef enum {
    LOCATION_NONE = 0,   //nothing
    LOCATION_GET,        //获取用户信息
    LOCATION_SHOW,       //显示用户位置
    LOCATION_GET_SHOW    //获取位置信息和显示用户位置
} LocationType;
*/

@interface UZBMap ()
<BMKGeneralDelegate, BMKMapViewDelegate, BMKLocationServiceDelegate, BMKGeoCodeSearchDelegate, BMKRouteSearchDelegate, BMKPoiSearchDelegate, BMKBusLineSearchDelegate, BMKSuggestionSearchDelegate, MovingAnimationDelegate, BMKOfflineMapDelegate> {
    //基础地图
    BMKMapView *_baiduMapView;
    //定位
    BMKLocationService *_locService;
    NSInteger startLocationCbid, getLocFromAddrCbid, getAddrFromLoc;
    BOOL shouldAutoStop, locationStarted, openShow, openSetCenter, showCurrentUserLoc;
    //LocationType locType;
    //搜索对象
    UZBMKGeoCodeSearch *_geoSearch;
    //监听地图事件
    NSInteger longPressCbid, viewChangeCbid, singleTapCbid, dubbleTapCbid;
    //大头针
    NSInteger setBubbleCbid, addBillboardCbid;
    //覆盖物
    NSMutableDictionary *_allOverlays;
    //路线搜索
    NSInteger searcRouteCbid, drawRouteCbid;
    NSMutableDictionary *_plans;
    NSMutableDictionary *_allRoutes, *_routeNodeSet;
    //公交搜索
    UZBMKPoiSearch *_poisearch;
    NSInteger getBusRouteCbid, drawBusRouteCbid;
    NSMutableDictionary *_allBusRoutes, *_allBusNodeSet;
    //城市内搜索
    NSInteger searchIncityCbid, searchNearByCbid, searchInboundsCbid, autoCompleteCbid;
    //移动标注
    NSMutableDictionary *_allMovingAnno;
    NSInteger moveAnnoCbid;
    //离线地图功能
    BMKOfflineMap *_offlineMap;
    NSInteger offlineListenerCbid;
}

@property (nonatomic, strong) BMKOfflineMap *offlineMap;
@property (nonatomic, strong) BMKMapView *baiduMapView;
@property (nonatomic, strong) BMKLocationService *locService;
@property (nonatomic, strong) UZBMKGeoCodeSearch *geoSearch;
@property (nonatomic, strong) NSMutableDictionary *allOverlays, *allRoutes, *allBusRoutes, *routeNodeSet, *allBusNodeSet;
@property (nonatomic, strong) NSDictionary *overlayStyles;
@property (nonatomic, strong) NSMutableDictionary *plans;
@property (nonatomic, strong) UZBMKPoiSearch *poisearch;
@property (nonatomic, strong) CADisplayLink *timerAnnoMove;
@property (nonatomic, strong) NSMutableDictionary *allMovingAnno;

@end

@implementation UZBMap

@synthesize offlineMap = _offlineMap;
@synthesize baiduMapView = _baiduMapView;
@synthesize locService = _locService;
@synthesize geoSearch = _geoSearch;
@synthesize allOverlays = _allOverlays;
@synthesize overlayStyles;
@synthesize plans = _plans;
@synthesize allRoutes = _allRoutes;
@synthesize poisearch = _poisearch;
@synthesize allBusRoutes = _allBusRoutes;
@synthesize routeNodeSet = _routeNodeSet;
@synthesize allBusNodeSet = _allBusNodeSet;
@synthesize timerAnnoMove;
@synthesize allMovingAnno = _allMovingAnno;

#pragma mark -
#pragma mark lifCycle
#pragma mark -

- (void)dispose {
    [self close:nil];
}

- (void)initMapSDK {
    if (![UZAppUtils globalValueForKey:@"BMKMapManager"]) {
        NSDictionary *feature_location = [self getFeatureByName:@"bMap"];
        NSString *ios_api_key = [feature_location stringValueForKey:@"ios_api_key" defaultValue:nil];
        if (!ios_api_key || ios_api_key.length==0) {
            NSDictionary *feature_map = [self getFeatureByName:@"baiduLocation"];
            ios_api_key = [feature_map stringValueForKey:@"ios_api_key" defaultValue:nil];
        }
        if (!ios_api_key || ios_api_key.length==0) {
            NSDictionary *feature_map = [self getFeatureByName:@"baiduMap"];
            ios_api_key = [feature_map stringValueForKey:@"ios_api_key" defaultValue:nil];
        }
        if (!ios_api_key) {
            ios_api_key = [feature_location stringValueForKey:@"apiKey" defaultValue:nil];
        }
        if (!ios_api_key) {
            ios_api_key = @"key";
        }
        BMKMapManager *g_mapSDKManager = [[BMKMapManager alloc] init];
        [g_mapSDKManager start:ios_api_key generalDelegate:self];
        [UZAppUtils setGlobalValue:g_mapSDKManager forKey:@"BMKMapManager"];
    }
}

- (void)initLocal {
    if (_locService) {
        [_locService stopUserLocationService];
    } else {
        _locService = [UZAppUtils globalValueForKey:@"BMKMapLocal"];
        if (!_locService) {
            _locService = [[BMKLocationService alloc] init];
            [UZAppUtils setGlobalValue:_locService forKey:@"BMKMapLocal"];
        } else {
            [_locService stopUserLocationService];
        }
    }
    _locService.delegate = self;
}

- (id)initWithUZWebView:(UZWebView *)webView_ {
    self = [super initWithUZWebView:webView_];
    if (self != nil) {
        //打开地图管理器
        [self initMapSDK];
        startLocationCbid = -1;
        openShow = NO;
        openSetCenter = NO;
        longPressCbid = -1;
        viewChangeCbid = -1;
        singleTapCbid = -1;
        dubbleTapCbid = -1;
        addBillboardCbid = -1;
        offlineListenerCbid = -1;
        _allOverlays = [NSMutableDictionary dictionaryWithCapacity:1];
        _allRoutes = [NSMutableDictionary dictionaryWithCapacity:1];
        _plans = [NSMutableDictionary dictionaryWithCapacity:1];
        _allBusRoutes = [NSMutableDictionary dictionaryWithCapacity:1];
        _routeNodeSet = [NSMutableDictionary dictionaryWithCapacity:1];
        _allBusNodeSet = [NSMutableDictionary dictionaryWithCapacity:1];
        self.timerAnnoMove = nil;
    }
    return self;
}

#pragma mark-
#pragma mark 基础类接口
#pragma mark-

- (void)open:(NSDictionary *)paramsDict_ {
   if (_baiduMapView) {
        [[_baiduMapView superview] bringSubviewToFront:_baiduMapView];
       _baiduMapView.hidden = NO;
        return;
    }
    NSString *fixedOnName = [paramsDict_ stringValueForKey:@"fixedOn" defaultValue:nil];
    UIView *superView = [self getViewByName:fixedOnName];
    NSDictionary *rectInfo = [paramsDict_ dictValueForKey:@"rect" defaultValue:@{}];
    float orgX = [rectInfo floatValueForKey:@"x" defaultValue:0];
    float orgY = [rectInfo floatValueForKey:@"y" defaultValue:0];
    float viewW = [rectInfo floatValueForKey:@"w" defaultValue:superView.bounds.size.width];
    float viewH = [rectInfo floatValueForKey:@"h" defaultValue:superView.bounds.size.height];
    float zoomLevel = [paramsDict_ floatValueForKey:@"zoomLevel" defaultValue:10];
    BOOL isShow = [paramsDict_ boolValueForKey:@"showUserLocation" defaultValue:YES];
    BOOL fixed = [paramsDict_ boolValueForKey:@"fixed" defaultValue:YES];
    
    //打开地图视图
    CGRect viewRect = CGRectMake(orgX, orgY, viewW, viewH);
    _baiduMapView = [[BMKMapView alloc]init];
    _baiduMapView.frame = viewRect;
    _baiduMapView.delegate = self;
    _baiduMapView.showsUserLocation = isShow;
    _baiduMapView.zoomLevel = zoomLevel;
    [self addSubview:_baiduMapView fixedOn:fixedOnName fixed:fixed];
    
    //设置地图中心点
    NSDictionary *centerInfo = [paramsDict_ dictValueForKey:@"center" defaultValue:@{}];
    if (centerInfo.count > 0) {
        float centerLon = [centerInfo floatValueForKey:@"lon" defaultValue:360];
        float centerLat = [centerInfo floatValueForKey:@"lat" defaultValue:360];
        if ([self isValidLon:centerLon lat:centerLat]) {
            CLLocationCoordinate2D coord2d;
            coord2d.latitude = centerLat;
            coord2d.longitude = centerLon;
            [_baiduMapView setCenterCoordinate:coord2d animated:NO];
        }
        openSetCenter = YES;
    } else {
        openSetCenter = NO;
    }
    
    //添加手势,屏蔽右滑关闭窗口事件
    UIPanGestureRecognizer *singleTap = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleTap:)];
    singleTap.cancelsTouchesInView = NO;
    [_baiduMapView addGestureRecognizer:singleTap];
    
    //设置父滚动视图的canCancelContentTouches
    UIWebView *webView = (UIWebView *)[self getViewByName:fixedOnName];
    id superViewObj = [webView superview];
    if (!fixed && [superViewObj isKindOfClass:[UIScrollView class]]) {
        UIScrollView *scrollView = (UIScrollView *)superViewObj;
        scrollView.canCancelContentTouches = NO;
    }

    //显示用户位置
    if (isShow) {
        NSDictionary *showInfo = [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] forKey:@"openShow"];
        [self showUserLocation:showInfo];
    }
    //回调
    [_baiduMapView viewWillAppear];
    NSInteger openCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    if (_baiduMapView && openCbid>=0 && [UZAppUtils globalValueForKey:@"BMKMapManager"]){
        [self sendResultEventWithCallbackId:openCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] forKey:@"status"] errDict:nil doDelete:YES];
    } else {
        [self sendResultEventWithCallbackId:openCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:nil doDelete:YES];
    }
}

- (void)setRect:(NSDictionary *)paramsDict_ {
    if (!_baiduMapView) {
        return;
    }
    UIView *superView = [self.baiduMapView superview];
    NSDictionary *rectInfo = [paramsDict_ dictValueForKey:@"rect" defaultValue:@{}];
    float orgX = [rectInfo floatValueForKey:@"x" defaultValue:superView.frame.origin.x];
    float orgY = [rectInfo floatValueForKey:@"y" defaultValue:superView.frame.origin.y];
    float viewW = [rectInfo floatValueForKey:@"w" defaultValue:superView.bounds.size.width];
    float viewH = [rectInfo floatValueForKey:@"h" defaultValue:superView.bounds.size.height];
    CGRect newRect = CGRectMake(orgX, orgY, viewW, viewH);
    [_baiduMapView setFrame:newRect];
}

- (void)setScaleBar:(NSDictionary *)paramsDict_ {
    BOOL show = [paramsDict_ boolValueForKey:@"show" defaultValue:NO];
    if (show) {
        _baiduMapView.showMapScaleBar = YES;
    } else {
        _baiduMapView.showMapScaleBar = NO;
    }
    NSDictionary *position = [paramsDict_ dictValueForKey:@"position" defaultValue:@{}];
    float x = [position floatValueForKey:@"x" defaultValue:0];
    float y = [position floatValueForKey:@"y" defaultValue:0];
    _baiduMapView.mapScaleBarPosition = CGPointMake(x, y);
}

- (void)setCompass:(NSDictionary *)paramsDict_ {
    NSDictionary *position = [paramsDict_ dictValueForKey:@"position" defaultValue:@{}];
    CGSize compassSize = _baiduMapView.compassSize;
    float x = [position floatValueForKey:@"x" defaultValue:compassSize.width/2.0];
    float y = [position floatValueForKey:@"y" defaultValue:compassSize.height/2.0];
    x -= compassSize.width/2.0;
    y -= compassSize.height/2.0;
    _baiduMapView.compassPosition = CGPointMake(x, y);
}

- (void)setTraffic:(NSDictionary *)paramsDict_ {
    BOOL show = [paramsDict_ boolValueForKey:@"traffic" defaultValue:YES];
    [_baiduMapView setTrafficEnabled:show];
}

- (void)setHeatMap:(NSDictionary *)paramsDict_ {
    BOOL show = [paramsDict_ boolValueForKey:@"heatMap" defaultValue:YES];
    [_baiduMapView setBaiduHeatMapEnabled:show];
}

- (void)setBuilding:(NSDictionary *)paramsDict_ {
    BOOL show = [paramsDict_ boolValueForKey:@"building" defaultValue:YES];
    [_baiduMapView setBuildingsEnabled:show];
}

- (void)close:(NSDictionary *)paramsDict_ {
    if (_allMovingAnno) {
        [_allMovingAnno removeAllObjects];
        self.allMovingAnno = nil;
    }
    if (_baiduMapView){
        _baiduMapView.showsUserLocation = NO;
        _baiduMapView.delegate = nil;
        [_baiduMapView removeFromSuperview];
        self.baiduMapView = nil;
    }
    if (_locService){
        [_locService stopUserLocationService];
        _locService.delegate = nil;
        self.locService = nil;
    }
    if (_geoSearch) {
        _geoSearch.delegate = nil;
        self.geoSearch = nil;
    }
    if (setBubbleCbid >= 0) {
        [self deleteCallback:setBubbleCbid];
    }
    if (_allOverlays) {
        [_allOverlays removeAllObjects];
        self.allOverlays = nil;
    }
    if (_allRoutes) {
        [_allRoutes removeAllObjects];
        self.allRoutes = nil;
    }
    if (_plans) {
        self.plans = nil;
    }
    if (_poisearch) {
        _poisearch.delegate = nil;
        self.poisearch = nil;
    }
    if (_allBusRoutes) {
        [_allBusRoutes removeAllObjects];
        self.allBusRoutes = nil;
    }
    if (_routeNodeSet) {
        [_routeNodeSet removeAllObjects];
        self.routeNodeSet = nil;
    }
    if (_allBusNodeSet) {
        [_allBusNodeSet removeAllObjects];
        self.allBusNodeSet = nil;
    }
    if (timerAnnoMove) {
        [timerAnnoMove invalidate];
        self.timerAnnoMove = nil;
    }
    if (_offlineMap) {
        _offlineMap.delegate = nil;
        self.offlineMap = nil;
    }
}

- (void)show:(NSDictionary *)paramsDict_ {
    if (_baiduMapView) {
        self.baiduMapView.hidden = NO;
    }
}

- (void)hide:(NSDictionary *)paramsDict_ {
    if (_baiduMapView) {
        self.baiduMapView.hidden = YES;
    }
}

- (void)getLocation:(NSDictionary *)paramsDict_ {
    startLocationCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    shouldAutoStop = [paramsDict_ boolValueForKey:@"autoStop" defaultValue:YES];
    [self initLocal];
    NSString *accuracyString = [[paramsDict_ stringValueForKey:@"accuracy" defaultValue:@"100m"] lowercaseString];
    CLLocationDistance distanceFilter = [paramsDict_ floatValueForKey:@"filter" defaultValue:1.0];
    if ([accuracyString isEqual:@"10m"]) {
        _locService.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    } else if ([accuracyString isEqual:@"100m"]) {
        _locService.desiredAccuracy = kCLLocationAccuracyHundredMeters;
    } else if ([accuracyString isEqual:@"1km"]) {
        _locService.desiredAccuracy = kCLLocationAccuracyKilometer;
    } else if ([accuracyString isEqual:@"3km"]) {
        _locService.desiredAccuracy = kCLLocationAccuracyThreeKilometers;
    } else {
        _locService.desiredAccuracy = kCLLocationAccuracyBest;
    }
    _locService.distanceFilter = distanceFilter;
    NSString *File = [[NSBundle mainBundle] pathForResource:@"Info" ofType:@"plist"];
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithContentsOfFile:File];
    NSArray *backAry = [dict objectForKey:@"UIBackgroundModes"];
    if ([backAry containsObject:@"location"]) {
        _locService.allowsBackgroundLocationUpdates = YES;
        _locService.pausesLocationUpdatesAutomatically = NO;
    }
    [_locService startUserLocationService];
    locationStarted = YES;
}

- (void)stopLocation:(NSDictionary *)paramsDict_ {
    locationStarted = NO;
    [_locService stopUserLocationService];
}

- (void)getCoordsFromName:(NSDictionary *)paramsDict_ {
    getLocFromAddrCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSString *city = [paramsDict_ stringValueForKey:@"city" defaultValue:nil];
    if (!city || city.length==0) {
        return;
    }
    NSString *addr =[paramsDict_ stringValueForKey:@"address" defaultValue:nil];
    if (!addr || addr.length==0) {
        return;
    }
    if (!_geoSearch) {
        _geoSearch = [[UZBMKGeoCodeSearch alloc]init];
        _geoSearch.delegate=self;
    }
    _geoSearch.city = city;
    _geoSearch.address = addr;
    BMKGeoCodeSearchOption *addrInfo = [[BMKGeoCodeSearchOption alloc]init];
    addrInfo.address = addr;
    addrInfo.city = city;
    BOOL isSearch = [self.geoSearch geoCode:addrInfo];
    if (!isSearch) {
        //[self sendResultEventWithCallbackId:getLocFromAddrCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
    }
}

- (void)getNameFromCoords:(NSDictionary *)paramsDict_ {
    getAddrFromLoc = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    float lon = [paramsDict_ floatValueForKey:@"lon" defaultValue:360];
    float lat = [paramsDict_ floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:lon lat:lat]) {
        return;
    }
    CLLocationCoordinate2D coord = (CLLocationCoordinate2D){0, 0};
    coord.longitude = lon;
    coord.latitude = lat;
    if (!_geoSearch) {
        _geoSearch = [[UZBMKGeoCodeSearch alloc]init];
        _geoSearch.delegate = self;
    }
    BMKReverseGeoCodeOption *locInfo = [[BMKReverseGeoCodeOption alloc]init];
    locInfo.reverseGeoPoint = coord;
    BOOL isSearch = [self.geoSearch reverseGeoCode:locInfo];
    if (!isSearch) {
        isSearch = [self.geoSearch reverseGeoCode:locInfo];
    }
}

- (void)getDistance:(NSDictionary *)paramsDict_ {
    NSInteger getDistanceCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSDictionary *startInfo = [paramsDict_ dictValueForKey:@"start" defaultValue:@{}];
    NSDictionary *endInfo = [paramsDict_ dictValueForKey:@"end" defaultValue:@{}];
    float startLon = [startInfo floatValueForKey:@"lon" defaultValue:360];
    float startLat = [startInfo floatValueForKey:@"lat" defaultValue:360];
    float endLon = [endInfo floatValueForKey:@"lon" defaultValue:360];
    float endLat = [endInfo floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:startLon lat:startLat]) {
        return;
    }
    if (![self isValidLon:endLon lat:endLat]) {
        return;
    }
    CLLocationCoordinate2D coors[2] = {0};
    coors[0].latitude = startLat;
    coors[0].longitude = startLon;
    coors[1].latitude = endLat;
    coors[1].longitude = endLon;
    BMKMapPoint pStart = BMKMapPointForCoordinate(coors[0]);
    BMKMapPoint pEnd = BMKMapPointForCoordinate(coors[1]);
    CLLocationDistance dis = BMKMetersBetweenMapPoints(pStart, pEnd);
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
    if (dis > 0) {
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:[NSNumber numberWithLongLong:dis] forKey:@"distance"];
    } else {
        [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
    }
    [self sendResultEventWithCallbackId:getDistanceCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)showUserLocation:(NSDictionary *)paramsDict_ {
    openShow = [paramsDict_ boolValueForKey:@"openShow" defaultValue:NO];
    BOOL showsUserLocation = [paramsDict_ boolValueForKey:@"isShow" defaultValue:YES];
    NSString *trackType = [paramsDict_ stringValueForKey:@"trackingMode" defaultValue:@"none"];
    if (trackType.length == 0) {
        trackType = @"none";
    }
    BMKUserTrackingMode trackmode = BMKUserTrackingModeNone;
    if ([trackType isEqualToString:@"compass"]) {
        trackmode = BMKUserTrackingModeFollowWithHeading;
    } else if ([trackType isEqualToString:@"follow"]) {
        trackmode = BMKUserTrackingModeFollow;
    }
    showCurrentUserLoc = YES;
    [self initLocal];
    [_locService stopUserLocationService];
    [_locService startUserLocationService];
    _baiduMapView.userTrackingMode = trackmode;//设置定位的状态
    _baiduMapView.showsUserLocation = NO;
    if (showsUserLocation) {
        _baiduMapView.showsUserLocation = YES;
    } else {
        _baiduMapView.showsUserLocation = NO;
    }
}

- (void)setCenter:(NSDictionary *)paramsDict_ {
    if (!_baiduMapView) {
        return;
    }
    NSDictionary *centerInfo = [paramsDict_ dictValueForKey:@"coords" defaultValue:@{}];
    float longitude = [centerInfo floatValueForKey:@"lon" defaultValue:360];
    float latitude = [centerInfo floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:longitude lat:latitude]) {
        return;
    }
    CLLocationCoordinate2D location2D ;
    location2D.longitude = longitude;
    location2D.latitude = latitude;
    BOOL animation = [paramsDict_ boolValueForKey:@"animation" defaultValue:YES];
    [_baiduMapView setCenterCoordinate:location2D animated:animation];
}

- (void)getCenter:(NSDictionary *)paramsDict_ {
    NSInteger getDistanceCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    float lat = self.baiduMapView.centerCoordinate.latitude;
    float lon = self.baiduMapView.centerCoordinate.longitude;
    [sendDict setObject:[NSNumber numberWithFloat:lat] forKey:@"lat"];
    [sendDict setObject:[NSNumber numberWithFloat:lon] forKey:@"lon"];
    [self sendResultEventWithCallbackId:getDistanceCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)setZoomLevel:(NSDictionary *)paramsDict_ {
    if (!_baiduMapView) {
        return;
    }
    int zoomLevel = [paramsDict_ floatValueForKey:@"level" defaultValue:10];
    if (zoomLevel > 18) {
        zoomLevel = 18;
    }
    if (zoomLevel < 3) {
        zoomLevel = 3;
    }
    [_baiduMapView setZoomLevel:zoomLevel];
}

- (void)getZoomLevel:(NSDictionary *)paramsDict_ {
    NSInteger getZoomLevelCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    float level = self.baiduMapView.zoomLevel;
    [sendDict setObject:[NSNumber numberWithFloat:level] forKey:@"level"];
    [self sendResultEventWithCallbackId:getZoomLevelCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)setMapAttr:(NSDictionary *)paramsDict_ {
    if (!_baiduMapView) {
        return;
    }
    BOOL zoomEnable = [paramsDict_ boolValueForKey:@"zoomEnable" defaultValue:YES];
    [_baiduMapView setZoomEnabled:zoomEnable];
    BOOL scrollEnable = [paramsDict_ boolValueForKey:@"scrollEnable" defaultValue:YES];
    [_baiduMapView setScrollEnabled:scrollEnable];
    //设置地图类型
    NSString *mapType = [paramsDict_ stringValueForKey:@"type" defaultValue:@"standard"];
    BMKMapType realMapType = BMKMapTypeStandard;
    if ([mapType isEqualToString:@"trafficOn"]) {
        ///< 标准地图
        _baiduMapView.trafficEnabled = YES;
        realMapType = BMKMapTypeStandard;
    } else if ([mapType isEqualToString:@"satellite"]) {
        ///< 卫星地图
        _baiduMapView.trafficEnabled = NO;
        realMapType = BMKMapTypeSatellite;
    } else if ([mapType isEqualToString:@"trafAndsate"]) {
        ///< 实时路况和卫星地图
        _baiduMapView.trafficEnabled = YES;
        realMapType = BMKMapTypeSatellite;
    } else {///< 标准地图
        _baiduMapView.trafficEnabled = NO;
        realMapType = BMKMapTypeStandard;
    }
    [_baiduMapView setMapType:realMapType];
}

- (void)setRotation:(NSDictionary *)paramsDict_ {
    if (!_baiduMapView) {
        return;
    }
    float rotation = [paramsDict_ floatValueForKey:@"degree" defaultValue:0];
    if (rotation > 180) {
        rotation = 180;
    }
    if (rotation < -180) {
        rotation = -180;
    }
    //地图旋转角度，在手机上当前可使用的范围为－180～180度
    self.baiduMapView.rotation = rotation;
}

- (void)setOverlook:(NSDictionary *)paramsDict_ {
    if (!_baiduMapView) {
        return;
    }
    float overlook = [paramsDict_ floatValueForKey:@"degree" defaultValue:0];
    if (overlook > 0) {
        overlook = 0 ;
    }
    if (overlook < -45) {
        overlook = -45;
    }
    //地图俯视角度，在手机上当前可使用的范围为－45～0度
    self.baiduMapView.overlooking = overlook;
}

- (void)setRegion:(NSDictionary *)paramsDict_ {
    float lbLon = [paramsDict_ floatValueForKey:@"lbLon" defaultValue:360];
    float lbLat = [paramsDict_ floatValueForKey:@"lbLat" defaultValue:360];
    float rtLon = [paramsDict_ floatValueForKey:@"rtLon" defaultValue:360];
    float rtLat = [paramsDict_ floatValueForKey:@"rtLat" defaultValue:360];
    if (![self isValidLon:lbLon lat:lbLat]) {
        return;
    }
    if (![self isValidLon:rtLon lat:rtLat]) {
        return;
    }
    BOOL animation = [paramsDict_ boolValueForKey:@"animation" defaultValue:YES];
    float centerLon, centerLat;
    if (lbLon>0 && rtLon<0) {
        return;
    } else {
        centerLon = (lbLon + rtLon)/2.0;
    }
    if (lbLat>0 && rtLat<0) {
        return;
    } else {
        centerLat = (lbLat + rtLat)/2.0;
    }
    
    CLLocationCoordinate2D centerCoords = CLLocationCoordinate2DMake(centerLat, centerLon);
    double latDelta, lonDelta;
    if (lbLat>0 && rtLat<0) {
        return;
    } else {
        latDelta = rtLat - lbLat;
    }
    if (lbLon>0 && rtLon<0) {
        return;
    } else {
        lonDelta = rtLon - lbLon;
    }
    BMKCoordinateSpan span;
    span.latitudeDelta = latDelta;
    span.longitudeDelta = lonDelta;
    BMKCoordinateRegion region = BMKCoordinateRegionMake(centerCoords, span);
    [_baiduMapView setRegion:region animated:animation];
}

- (void)getRegion:(NSDictionary *)paramsDict_ {
    NSInteger getRegionId = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    BMKCoordinateRegion region = _baiduMapView.region;
    CLLocationCoordinate2D center = region.center;
    double lat = center.latitude;
    double lon = center.longitude;
    BMKCoordinateSpan span = region.span;
    double spanLat = span.latitudeDelta;
    double spanLon = span.longitudeDelta;
    double lbLon, lbLat, rtLon, rtLat;
    lbLon = lon - spanLon;
    lbLat = lat - spanLat;
    rtLon = lon + spanLon;
    rtLat = lat + spanLat;
    //回调
    BOOL status;
    if ([self isValidLon:lbLon lat:lbLat]) {
        status = YES;
    } else {
        status = NO;
    }
    if ([self isValidLon:rtLon lat:rtLat]) {
        status = YES;
    } else {
        status = NO;
    }
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
    [sendDict setObject:[NSNumber numberWithDouble:lbLat] forKey:@"lbLat"];
    [sendDict setObject:[NSNumber numberWithDouble:lbLon] forKey:@"lbLon"];
    [sendDict setObject:[NSNumber numberWithDouble:rtLat] forKey:@"rtLat"];
    [sendDict setObject:[NSNumber numberWithDouble:rtLon] forKey:@"rtLon"];
    [sendDict setObject:[NSNumber numberWithBool:status] forKey:@"status"];
    [self sendResultEventWithCallbackId:getRegionId dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)transCoords:(NSDictionary *)paramsDict_ {
    NSInteger cbId = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    float longitude = [paramsDict_ floatValueForKey:@"lon" defaultValue:360];
    float latitude = [paramsDict_ floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:longitude lat:latitude]) {
        [self sendResultEventWithCallbackId:cbId dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:@{@"code":[NSNumber numberWithInt:1]} doDelete:YES];
        return;
    }
    NSString *type = [paramsDict_ stringValueForKey:@"type" defaultValue:@"common"];
    if (![type isKindOfClass:[NSString class]] || type.length==0) {
        type = @"common";
    }
    BMK_COORD_TYPE cooType;
    if ([type isEqualToString:@"common"]) {
        cooType = BMK_COORDTYPE_COMMON;
    } else {
        cooType = BMK_COORDTYPE_GPS;
    }
    NSDictionary *baidudict = BMKConvertBaiduCoorFrom(CLLocationCoordinate2DMake(latitude, longitude),cooType);
    CLLocationCoordinate2D coord = BMKCoorDictionaryDecode(baidudict);
    NSNumber *xstr = [NSNumber numberWithDouble:coord.longitude];
    NSNumber *ystr = [NSNumber numberWithDouble:coord.latitude];
    if (xstr && ystr) {
        NSArray *key = [NSArray arrayWithObjects:@"lon",@"lat",@"status", nil];
        NSArray *value = [NSArray arrayWithObjects:xstr, ystr,[NSNumber numberWithBool:YES], nil];
        NSDictionary *callBackDic = [NSDictionary dictionaryWithObjects:value forKeys:key];
        [self sendResultEventWithCallbackId:cbId dataDict:callBackDic errDict:nil doDelete:YES];
    } else {
        [self sendResultEventWithCallbackId:cbId dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:@{@"code":[NSNumber numberWithInt:2]} doDelete:YES];
    }
}

- (void)zoomIn:(NSDictionary *)paramsDict_ {
    if (_baiduMapView) {
        [_baiduMapView zoomIn];
    }
}

- (void)zoomOut:(NSDictionary *)paramsDict_ {
    if (_baiduMapView) {
        [_baiduMapView zoomOut];
    }
}

- (void)isPolygonContantsPoint:(NSDictionary *)paramsDict_ {
    NSInteger cbcontantId = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSArray *arrSome = [paramsDict_ arrayValueForKey:@"points" defaultValue:nil];
    if (arrSome.count == 0) {
        [self sendResultEventWithCallbackId:cbcontantId dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] forKey:@"status"] errDict:nil doDelete:YES];
        return;
    }
    NSDictionary *targetInfo = [paramsDict_ dictValueForKey:@"point" defaultValue:@{}];
    if (!targetInfo || targetInfo.count==0) {
        [self sendResultEventWithCallbackId:cbcontantId dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] forKey:@"status"] errDict:nil doDelete:YES];
        return;
    }
    float targetX = [targetInfo floatValueForKey:@"lat" defaultValue:360];
    float targetY = [targetInfo floatValueForKey:@"lon" defaultValue:360];
   
    NSInteger pointCount = [arrSome count];
    /*
    CLLocationCoordinate2D coorAry[200] = {0};
    for (int i=0; i<pointCount; i++){
        NSDictionary *item = [arrSome objectAtIndex:i];
        if ([item isKindOfClass:[NSDictionary class]]) {
            float lon = [item floatValueForKey:@"lon" defaultValue:360];
            float lat = [item floatValueForKey:@"lat" defaultValue:360];
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            coorAry[i].longitude = lon;
            coorAry[i].latitude = lat;
        }
    }
    BMKPolygon *polygon = [BMKPolygon polygonWithCoordinates:coorAry count:pointCount];
     */
    CLLocationCoordinate2D allPoint[200]= {0};
    for (int i=0; i<pointCount; i++){
        NSDictionary *item = [arrSome objectAtIndex:i];
        float lon = [item floatValueForKey:@"lon" defaultValue:360];
        float lat = [item floatValueForKey:@"lat" defaultValue:360];
        if (![self isValidLon:lon lat:lat]) {
            continue;
        }
        CLLocationCoordinate2D tempPoint = CLLocationCoordinate2DMake(lat, lon);
        allPoint[i] = tempPoint;
    }

    CLLocationCoordinate2D targetPoint = CLLocationCoordinate2DMake(targetX, targetY);
    BOOL is = BMKPolygonContainsCoordinate(targetPoint, allPoint, pointCount);
    [self sendResultEventWithCallbackId:cbcontantId dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:is] forKey:@"status"] errDict:nil doDelete:YES];
}

- (void)addEventListener:(NSDictionary *)paramsDict_ {
    NSString *nameStr = [paramsDict_ stringValueForKey:@"name" defaultValue:nil];
    if (nameStr.length == 0) {
        return;
    }
    if ([nameStr isEqualToString:@"longPress"]) {//长按监听
        if (longPressCbid != -1) {
            [self deleteCallback:longPressCbid];
        }
        if (longPressCbid != -1) {
            [self deleteCallback:longPressCbid];
        }
        longPressCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    } else if ([nameStr isEqualToString:@"viewChange"]) {//视角改变监听
        if (viewChangeCbid != -1) {
            [self deleteCallback:viewChangeCbid];
        }
        viewChangeCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    } else if ([nameStr isEqualToString:@"click"]) {//单击监听
        if (singleTapCbid != -1) {
            [self deleteCallback:singleTapCbid];
        }
        singleTapCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    } else if ([nameStr isEqualToString:@"dbclick"]) {//双击监听
        if (dubbleTapCbid != -1) {
            [self deleteCallback:dubbleTapCbid];
        }
        dubbleTapCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    }
}

- (void)removeEventListener:(NSDictionary *)paramsDict_ {
    NSString *nameStr = [paramsDict_ stringValueForKey:@"name" defaultValue:nil];
    if (nameStr.length == 0) {
        return;
    }
    if ([nameStr isEqualToString:@"longPress"]) {//移除长按监听
        if (longPressCbid != -1) {
            [self deleteCallback:longPressCbid];
        }
        longPressCbid = -1;
    } else if ([nameStr isEqualToString:@"viewChange"]) {//移除视角改变监听
        if (viewChangeCbid >= 0) {
            [self deleteCallback:viewChangeCbid];
        }
        viewChangeCbid = -1;
    } else if ([nameStr isEqualToString:@"click"]) {//移除单击监听
        if (singleTapCbid != -1) {
            [self deleteCallback:singleTapCbid];
        }
        singleTapCbid = -1;
    } else if ([nameStr isEqualToString:@"dbclick"]) {//移除双击监听
        if (dubbleTapCbid != -1) {
            [self deleteCallback:dubbleTapCbid];
        }
        dubbleTapCbid = -1;
    }
}

#pragma mark-
#pragma mark 标注、气泡类接口
#pragma mark-

- (void)addAnnotations:(NSDictionary *)paramsDict_ {
    NSInteger addAnnCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSString *icon = [paramsDict_ stringValueForKey:@"icon" defaultValue:nil];
    BOOL draggable = [paramsDict_ boolValueForKey:@"draggable" defaultValue:NO];
    NSArray *annoAry = [paramsDict_ arrayValueForKey:@"annotations" defaultValue:nil];
    NSMutableArray *annotationsAry = [NSMutableArray arrayWithCapacity:1];
    for (NSDictionary *annoInfo in annoAry) {
        if (![annoInfo isKindOfClass:[NSDictionary class]] || annoInfo.count==0) {
            continue;
        }
        float lon = [annoInfo floatValueForKey:@"lon" defaultValue:360];
        float lat = [annoInfo floatValueForKey:@"lat" defaultValue:360];
        if (![self isValidLon:lon lat:lat]) {
            continue;
        }
        NSInteger annoId = [annoInfo integerValueForKey:@"id" defaultValue:-1];
        if (annoId < 0) {
            continue;
        }
        NSString *pinIcon = [annoInfo stringValueForKey:@"icon" defaultValue:nil];
        UZbMapAnnotation *annotation = [[UZbMapAnnotation alloc] init];
        CLLocationCoordinate2D coor;
        coor.longitude = lon;
        coor.latitude = lat;
        annotation.coordinate = coor;
        annotation.annoId = annoId;
        annotation.clickCbId = addAnnCbid;
        if ([pinIcon isKindOfClass:[NSString class]] && pinIcon.length>0) {
            annotation.pinImg = pinIcon;
        } else {
            annotation.pinImg = icon;
        }
        if ([annoInfo objectForKey:@"draggable"]) {
            annotation.draggable = [annoInfo boolValueForKey:@"draggable" defaultValue:NO];
        } else {
            annotation.draggable = draggable;
        }
        annotation.title = @"APICloud";
        annotation.isStyled = NO;
        annotation.type = ANNOTATION_MARKE;
        [annotationsAry addObject:annotation];
    }
    if (annotationsAry.count > 0) {
        [self.baiduMapView addAnnotations:annotationsAry];
    }
}

- (void)getAnnotationCoords:(NSDictionary *)paramsDict_ {
    NSString *setID = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![setID isKindOfClass:[NSString class]] || setID.length==0) {
        return;
    }
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    if (getCbid < 0) {
        return;
    }
    NSArray *annos = [self.baiduMapView annotations];
    for (UZbMapAnnotation *annoElem in annos) {
        if (annoElem.annoId == [setID intValue]) {
            CLLocationCoordinate2D coords = annoElem.coordinate;
            NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
            [sendDict setObject:[NSNumber numberWithDouble:coords.latitude] forKey:@"lat"];
            [sendDict setObject:[NSNumber numberWithDouble:coords.longitude] forKey:@"lon"];
            [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
            break;
        }
    }
}

- (void)setAnnotationCoords:(NSDictionary *)paramsDict_ {
    NSString *setID = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![setID isKindOfClass:[NSString class]] || setID.length==0) {
        return;
    }
    float lat = [paramsDict_ floatValueForKey:@"lat" defaultValue:360];
    float lon = [paramsDict_ floatValueForKey:@"lon" defaultValue:360];
    if (![self isValidLon:lon lat:lat]) {
        return;
    }
    NSArray *annos = [self.baiduMapView annotations];
    for (UZbMapAnnotation *annoElem in annos) {
        if (annoElem.annoId == [setID intValue]) {
            CLLocationCoordinate2D coords = CLLocationCoordinate2DMake(lat, lon);
            [annoElem setCoordinate:coords];
            [_baiduMapView mapForceRefresh];
            //CLLocationCoordinate2D center = _baiduMapView.centerCoordinate;
            //[_baiduMapView setCenterCoordinate:center];
            break;
        }
    }
}

- (void)annotationExist:(NSDictionary *)paramsDict_ {
    NSString *setID = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![setID isKindOfClass:[NSString class]] || setID.length==0) {
        return;
    }
    NSInteger cbExistID = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSArray *annos = [self.baiduMapView annotations];
    for (UZbMapAnnotation *annoElem in annos) {
        if (annoElem.annoId == [setID intValue]) {
            [self sendResultEventWithCallbackId:cbExistID dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] forKey:@"status"] errDict:nil doDelete:YES];
            break;
        }
    }
    [self sendResultEventWithCallbackId:cbExistID dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:nil doDelete:YES];
}

- (void)setBubble:(NSDictionary *)paramsDict_ {
    NSString *setID = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    NSString *bgImgStr = [paramsDict_ stringValueForKey:@"bgImg" defaultValue:nil];
    NSDictionary *contentInfo = [paramsDict_ dictValueForKey:@"content" defaultValue:@{}];
    NSDictionary *styleInfo = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    NSArray *annos = [self.baiduMapView annotations];
    if ([setID isKindOfClass:[NSString class]] && setID.length>0) {
        if (setBubbleCbid >= 0) {
            [self deleteCallback:setBubbleCbid];
        }
        setBubbleCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
        for (UZbMapAnnotation *annoElem in annos) {
            if (annoElem.annoId == [setID integerValue]) {
                annoElem.content = contentInfo;
                annoElem.styles = styleInfo;
                annoElem.title = @"";
                annoElem.isStyled = YES;
                annoElem.bubbleBgImg = bgImgStr;
                [self.baiduMapView removeAnnotation:annoElem];
                [self.baiduMapView addAnnotation:annoElem];
                break;
            }
        }
    }
}

- (void)popupBubble:(NSDictionary *)paramsDict_ {
    NSString *setID = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![setID isKindOfClass:[NSString class]] || setID.length==0) {
        return;
    }
    NSArray *annos = [self.baiduMapView annotations];
    for (UZbMapAnnotation *annoElem in annos){
        if (annoElem.annoId == [setID intValue]) {
            annoElem.popBubble = YES;
            [self.baiduMapView selectAnnotation:annoElem animated:YES];
            break;
        }
    }
}

- (void)closeBubble:(NSDictionary *)paramsDict_ {
    NSString *setID = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![setID isKindOfClass:[NSString class]] || setID.length==0) {
        return;
    }
    NSArray *annos = [self.baiduMapView annotations];
    for (UZbMapAnnotation *annoElem in annos){
        if (annoElem.annoId == [setID intValue]) {
            annoElem.popBubble = NO;
            [self.baiduMapView deselectAnnotation:annoElem animated:YES];
            break;
        }
    }
}

- (void)addBillboard:(NSDictionary *)paramsDict_ {
    NSString *setID = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    NSDictionary *coordsInfo = [paramsDict_ dictValueForKey:@"coords" defaultValue:@{}];
    NSString *bgImgStr = [paramsDict_ stringValueForKey:@"bgImg" defaultValue:nil];
    NSDictionary *contentInfo = [paramsDict_ dictValueForKey:@"content" defaultValue:@{}];
    NSDictionary *styleInfo = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    if (![setID isKindOfClass:[NSString class]]|| setID.length==0){
        return;
    }
    double lon = [coordsInfo floatValueForKey:@"lon" defaultValue:360];
    double lat = [coordsInfo floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:lon lat:lat] ) {
        return;
    }
    if (![bgImgStr isKindOfClass:[NSString class]] || bgImgStr.length==0) {
        return;
    }
    if (addBillboardCbid >= 0) {
        [self deleteCallback:addBillboardCbid];
    }
    addBillboardCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    UZbMapAnnotation *annotation = [[UZbMapAnnotation alloc] init];
    CLLocationCoordinate2D coor;
    coor.longitude = lon;
    coor.latitude = lat;
    annotation.coordinate = coor;
    annotation.annoId = [setID integerValue];
    annotation.pinImg = bgImgStr;
    annotation.draggable = NO;
    annotation.title = @"APICloud";
    annotation.isStyled = NO;
    annotation.content = contentInfo;
    annotation.styles = styleInfo;
    annotation.type = ANNOTATION_BILLBOARD;
    [self.baiduMapView addAnnotation:annotation];
}

- (void)addMobileAnnotations:(NSDictionary *)paramsDict_ {
    NSArray *annoAry = [paramsDict_ arrayValueForKey:@"annotations" defaultValue:nil];
    NSMutableArray *annotationsAry = [NSMutableArray arrayWithCapacity:1];
    for (NSDictionary *annoInfo in annoAry) {
        if (![annoInfo isKindOfClass:[NSDictionary class]] || annoInfo.count==0) {
            continue;
        }
        float lon = [annoInfo floatValueForKey:@"lon" defaultValue:360];
        float lat = [annoInfo floatValueForKey:@"lat" defaultValue:360];
        if (![self isValidLon:lon lat:lat]) {
            continue;
        }
        NSInteger annoId = [annoInfo integerValueForKey:@"id" defaultValue:-1];
        if (annoId < 0) {
            continue;
        }
        NSString *pinIcon = [annoInfo stringValueForKey:@"icon" defaultValue:nil];
        UZbMapAnnotation *annotation = [[UZbMapAnnotation alloc] init];
        CLLocationCoordinate2D coor;
        coor.longitude = lon;
        coor.latitude = lat;
        annotation.coordinate = coor;
        annotation.annoId = annoId;
        annotation.pinImg = pinIcon;
        annotation.title = @"APICloud";
        annotation.isStyled = NO;
        annotation.type = ANNOTATION_MOBILE;
        annotation.currentAngle = M_PI/2.0;
        [annotationsAry addObject:annotation];
    }
    if (annotationsAry.count > 0) {
        [self.baiduMapView addAnnotations:annotationsAry];
    }
}

- (void)moveAnnotation:(NSDictionary *)paramsDict_ {
    if (!_allMovingAnno) {
        _allMovingAnno = [NSMutableDictionary dictionaryWithCapacity:1];
    }
    NSDictionary *endInfo = [paramsDict_ dictValueForKey:@"end" defaultValue:@{}];
    if (endInfo.count == 0) {
        return;
    }
    float endlon = [endInfo floatValueForKey:@"lon" defaultValue:360];
    float endlat = [endInfo floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:endlon lat:endlat]) {
        return;
    }
    CLLocationCoordinate2D toCoords = CLLocationCoordinate2DMake(endlat, endlon);
    NSString *moveId = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![moveId isKindOfClass:[NSString class]] || moveId.length==0) {
        return;
    }
    if (moveAnnoCbid >= 0) {
        [self deleteCallback:moveAnnoCbid];
    }
    moveAnnoCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    float moveDuration = [paramsDict_ floatValueForKey:@"duration" defaultValue:1];
    CFTimeInterval thisStep = CACurrentMediaTime();
    NSArray *annos = [self.baiduMapView annotations];
    for (UZbMapAnnotation *annoElem in annos){
        if (annoElem.annoId == [moveId intValue]) {
            //获取当前坐标
            float startlon = annoElem.coordinate.longitude;
            float startlat = annoElem.coordinate.latitude;
            CLLocationCoordinate2D fromCoords = CLLocationCoordinate2DMake(startlat, startlon);
            annoElem.fromCoords = fromCoords;
            annoElem.toCoords = toCoords;
            //计算旋转方向
            float angle = -atan2(endlat - startlat, endlon - startlon);
            float currentAngle = annoElem.currentAngle;
            float rotateAngle;
            if (currentAngle > angle) {
                rotateAngle = M_PI - currentAngle + angle;
            } else {
                rotateAngle = -(M_PI + currentAngle - angle);
            }
            annoElem.currentAngle += rotateAngle;
            annoElem.moveDuration = moveDuration;
            annoElem.lastStep = thisStep;
            annoElem.timeOffset = 0;
            annoElem.delegate = self;
            //添加到移动队列
            [self.allMovingAnno setObject:annoElem forKey:moveId];
            //先做旋转动画
            BMKAnnotationView *pinAnnotationView = [_baiduMapView viewForAnnotation:annoElem];
            UIImageView *roteImg = (UIImageView *)[pinAnnotationView viewWithTag:986];
            void(^animationManager)(void) = ^(void) {
                CGAffineTransform transform = roteImg.transform;
                transform = CGAffineTransformRotate(transform, rotateAngle);
                roteImg.transform = transform;
            };
            //再做移动动画
            __weak UZBMap *OBJMap = self;
            void(^completion)(BOOL finished) = ^(BOOL finished) {
                if (OBJMap.timerAnnoMove == nil) {
                    [OBJMap.timerAnnoMove invalidate];
                    OBJMap.timerAnnoMove = [CADisplayLink displayLinkWithTarget:OBJMap selector:@selector(doStep)];
                    OBJMap.timerAnnoMove.frameInterval = 2;
                    NSRunLoop *mainRunLoop = [NSRunLoop currentRunLoop];
                    [OBJMap.timerAnnoMove addToRunLoop:mainRunLoop forMode:NSDefaultRunLoopMode];
                    [OBJMap.timerAnnoMove addToRunLoop:mainRunLoop forMode:UITrackingRunLoopMode];
                }
            };
            [UIView animateWithDuration:0.3 animations:animationManager completion:completion];
            break;
        }
    }
}

- (void)doStep {
    if (_allMovingAnno.count == 0) {
        [timerAnnoMove invalidate];
        self.timerAnnoMove = nil;
        return;
    }
    NSArray *allKey = [_allMovingAnno allKeys];
    for (NSString *annoKey in allKey) {
        UZbMapAnnotation *anno = [self.allMovingAnno objectForKey:annoKey];
        [anno moveStep];
    }
    [_baiduMapView mapForceRefresh];
}

- (void)removeAnnotations:(NSDictionary *)paramsDict_ {
    NSArray *idAry = [paramsDict_ arrayValueForKey:@"ids" defaultValue:nil];
    if (idAry.count > 0) {
        NSMutableArray *removeIdAry = [NSMutableArray arrayWithCapacity:1];
        for (int i=0; i<idAry.count; i++) {
            NSString *idStr = [NSString stringWithFormat:@"%@",[idAry objectAtIndex:i]];
            [removeIdAry addObject:idStr];
        }
        NSArray *annos = [self.baiduMapView annotations];
        NSMutableArray *willRemoveAnnos = [NSMutableArray arrayWithCapacity:1];
        for (UZbMapAnnotation *annoAry in annos) {
            NSString *annID = [NSString stringWithFormat:@"%ld",(long)annoAry.annoId];
            if ([removeIdAry containsObject:annID]) {
                [willRemoveAnnos addObject:annoAry];
            }
        }
        [self.baiduMapView removeAnnotations:willRemoveAnnos];
    }
}

#pragma mark-
#pragma mark 覆盖物类接口
#pragma mark-

- (void)addLine:(NSDictionary *)paramsDict_ {
    NSArray *pointAry = [paramsDict_ arrayValueForKey:@"points" defaultValue:nil];
    if (pointAry.count == 0) {
        return;
    }
    NSString *overlayIdStr = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![overlayIdStr isKindOfClass:[NSString class]] || overlayIdStr.length==0) {
        return;
    }
    id target = [self.allOverlays objectForKey:overlayIdStr];
    if (target) {
        [self.baiduMapView removeOverlay:target];
    }
    overlayStyles = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    NSInteger pointCount = [pointAry count];
    CLLocationCoordinate2D coorAry[500] = {0};
    for (int i=0; i<pointCount; i++){
        NSDictionary *item = [pointAry objectAtIndex:i];
        if ([item isKindOfClass:[NSDictionary class]]) {
            float lon = [item floatValueForKey:@"lon" defaultValue:360];
            float lat = [item floatValueForKey:@"lat" defaultValue:360];
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            coorAry[i].longitude = lon;
            coorAry[i].latitude = lat;
        }
    }
    UZBMKPolyline *line = [[UZBMKPolyline alloc]init];
    [line setPolylineWithCoordinates:coorAry count:pointCount];
    line.lineType = 0;
    [self.baiduMapView addOverlay:line];
    [self.allOverlays setObject:line forKey:overlayIdStr];
}

- (void)addPolygon:(NSDictionary *)paramsDict_ {
    NSArray *pointAry = [paramsDict_ arrayValueForKey:@"points" defaultValue:nil];
    if (pointAry.count == 0) {
        return;
    }
    NSString *overlayIdStr = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![overlayIdStr isKindOfClass:[NSString class]] || overlayIdStr.length==0) {
        return;
    }
    id target = [self.allOverlays objectForKey:overlayIdStr];
    if (target) {
        [self.baiduMapView removeOverlay:target];
    }
    overlayStyles = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    NSInteger pointCount = [pointAry count];
    CLLocationCoordinate2D coorAry[200] = {0};
    for (int i=0; i<pointCount; i++){
        NSDictionary *item = [pointAry objectAtIndex:i];
        if ([item isKindOfClass:[NSDictionary class]]) {
            float lon = [item floatValueForKey:@"lon" defaultValue:360];
            float lat = [item floatValueForKey:@"lat" defaultValue:360];
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            coorAry[i].longitude = lon;
            coorAry[i].latitude = lat;
        }
    }
    BMKPolygon *polygon = [BMKPolygon polygonWithCoordinates:coorAry count:pointCount];
    [self.baiduMapView addOverlay:polygon];
    [self.allOverlays setObject:polygon forKey:overlayIdStr];
}

- (void)addCircle:(NSDictionary *)paramsDict_ {
    NSDictionary *centerInfo = [paramsDict_ dictValueForKey:@"center" defaultValue:@{}];
    float lon = [centerInfo floatValueForKey:@"lon" defaultValue:360];
    float lat = [centerInfo floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:lon lat:lat]) {
        return;
    }
    float radius = [paramsDict_ floatValueForKey:@"radius" defaultValue:0];
    if (radius < 0) {
        return;
    }
    NSString *overlayIdStr = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![overlayIdStr isKindOfClass:[NSString class]] || overlayIdStr.length==0) {
        return;
    }
    id target = [self.allOverlays objectForKey:overlayIdStr];
    if (target) {
        [self.baiduMapView removeOverlay:target];
    }
    self.overlayStyles = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    CLLocationCoordinate2D coor;
    coor.longitude = lon;
    coor.latitude = lat;
    BMKCircle *circle = [BMKCircle circleWithCenterCoordinate:coor radius:radius];
    [self.baiduMapView addOverlay:circle];
    [self.allOverlays setObject:circle forKey:overlayIdStr];
}

- (void)addArc:(NSDictionary *)paramsDict_ {
    NSArray *pointAry = [paramsDict_ arrayValueForKey:@"points" defaultValue:nil];
    if (pointAry.count == 0) {
        return;
    }
    NSString *overlayIdStr = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![overlayIdStr isKindOfClass:[NSString class]] || overlayIdStr.length==0) {
        return;
    }
    id target = [self.allOverlays objectForKey:overlayIdStr];
    if (target) {
        [self.baiduMapView removeOverlay:target];
    }
    overlayStyles = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    NSInteger pointCount = [pointAry count];
    CLLocationCoordinate2D coorAry[200] = {0};
    for (int i=0; i<pointCount; i++){
        NSDictionary *item = [pointAry objectAtIndex:i];
        if ([item isKindOfClass:[NSDictionary class]]) {
            float lon = [item floatValueForKey:@"lon" defaultValue:360];
            float lat = [item floatValueForKey:@"lat" defaultValue:360];
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            coorAry[i].longitude = lon;
            coorAry[i].latitude = lat;
        }
    }
    BMKArcline *arcline = [BMKArcline arclineWithCoordinates:coorAry];
    [self.baiduMapView addOverlay:arcline];
    [self.allOverlays setObject:arcline forKey:overlayIdStr];
}

- (void)addImg:(NSDictionary *)paramsDict_ {
    NSString *overlayIdStr = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (![overlayIdStr isKindOfClass:[NSString class]] || overlayIdStr.length==0) {
        return;
    }
    id target = [self.allOverlays objectForKey:overlayIdStr];
    if (target) {
        [self.baiduMapView removeOverlay:target];
    }
    float lbLon = [paramsDict_ floatValueForKey:@"lbLon" defaultValue:360];
    float lbLat = [paramsDict_ floatValueForKey:@"lbLat" defaultValue:360];
    float rtLon = [paramsDict_ floatValueForKey:@"rtLon" defaultValue:360];
    float rtLat = [paramsDict_ floatValueForKey:@"rtLat" defaultValue:360];
    if (![self isValidLon:lbLon lat:lbLat]) {
        return;
    }
    if (![self isValidLon:rtLon lat:rtLat]) {
        return;
    }
    NSString *imgPath = [paramsDict_ stringValueForKey:@"imgPath" defaultValue:nil];
    if (![imgPath isKindOfClass:[NSString class]] || imgPath.length==0) {
        return;
    }
    float alpha = [paramsDict_ floatValueForKey:@"opacity" defaultValue:1];
    CLLocationCoordinate2D coords[2] = {0};
    coords[0].latitude = lbLat;
    coords[0].longitude = lbLon;
    coords[1].latitude = rtLat;
    coords[1].longitude = rtLon;
    BMKCoordinateBounds bound;
    bound.southWest = coords[0];
    bound.northEast = coords[1];
    UIImage *image = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:imgPath]];
    BMKGroundOverlay *ground = [BMKGroundOverlay groundOverlayWithBounds:bound icon:image];
    ground.alpha = alpha;
    [self.baiduMapView addOverlay:ground];
    [self.allOverlays setObject:ground forKey:overlayIdStr];
}

- (void)removeOverlay:(NSDictionary *)paramsDict_ {
    NSArray *idAry = [paramsDict_ arrayValueForKey:@"ids" defaultValue:nil];
    if (idAry.count == 0) {
        return;
    }
    for (id idstr in idAry) {
        NSString *idString = [NSString stringWithFormat:@"%@",idstr];
        id target = [self.allOverlays objectForKey:idString];
        if (target) {
            [self.baiduMapView removeOverlay:target];
        }
    }
}

#pragma mark -
#pragma mark 搜索类接口
#pragma mark -

- (void)searchRoute:(NSDictionary *)paramsDict_ {
    NSString *routeId = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (routeId.length == 0) {
        return;
    }
    searcRouteCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSString *routeType = [paramsDict_ stringValueForKey:@"type" defaultValue:@"transit"];
    NSDictionary *startNode = [paramsDict_ dictValueForKey:@"start" defaultValue:@{}];
    float startLon = [startNode floatValueForKey:@"lon" defaultValue:360];
    float startLat = [startNode floatValueForKey:@"lat" defaultValue:360];
    NSDictionary *endNode = [paramsDict_ dictValueForKey:@"end" defaultValue:@{}];
    float endLon = [endNode floatValueForKey:@"lon" defaultValue:360];
    float endLat = [endNode floatValueForKey:@"lat" defaultValue:360];
    CLLocationCoordinate2D startCoor = (CLLocationCoordinate2D){0,0};
    startCoor.longitude = startLon;
    startCoor.latitude = startLat;
    CLLocationCoordinate2D endCoor = (CLLocationCoordinate2D){0,0};
    endCoor.longitude = endLon;
    endCoor.latitude = endLat;
    //self.baiduMapView.centerCoordinate = endCoor;
    BMKPlanNode *start = [[BMKPlanNode alloc] init];
    if ([self isValidLon:startLon lat:startLat]) {
        start.pt = startCoor;
    }
    BMKPlanNode *end = [[BMKPlanNode alloc] init];
    if ([self isValidLon:endLon lat:endLat]) {
        end.pt = endCoor;
    }
    UZBMKRouteSearch *routeSearcher = [[UZBMKRouteSearch alloc] init];
    routeSearcher.delegate = self;
    routeSearcher.searchRouteId = routeId;
    NSInteger routeTypeValue = 0;
    if ([routeType isEqualToString:@"transit"]){
        routeTypeValue = 1;
    } else if ([routeType isEqualToString:@"walk"]){
        routeTypeValue = 2;
    }
    BOOL isSecceed;
    NSString *policy = nil;
    BMKDrivingPolicy drivePolicy = BMK_DRIVING_TIME_FIRST;
    BMKTransitPolicy transPlicy = BMK_TRANSIT_TIME_FIRST;
    if (routeTypeValue == 0) {
        policy = [paramsDict_ stringValueForKey:@"policy" defaultValue:@"ecar_time_first"];
        if ([policy isEqualToString:@"ecar_fee_first"]) {
            drivePolicy = BMK_DRIVING_FEE_FIRST;
        } else if ([policy isEqualToString:@"ecar_dis_first"]){
            drivePolicy = BMK_DRIVING_DIS_FIRST;
        } else if ([policy isEqualToString:@"ecar_time_first"]){
            drivePolicy = BMK_DRIVING_TIME_FIRST;
        } else if ([policy isEqualToString:@"ecar_avoid_first"]){
            drivePolicy = BMK_DRIVING_BLK_FIRST;
        } else {
            drivePolicy = BMK_DRIVING_TIME_FIRST;
        }
    } else if (routeTypeValue == 1) {
        policy = [paramsDict_ stringValueForKey:@"policy" defaultValue:@"ebus_time_first"];
        if ([policy isEqualToString:@"ebus_no_subway"]) {
            transPlicy = BMK_TRANSIT_NO_SUBWAY;
        } else if ([policy isEqualToString:@"ebus_time_first"]) {
            transPlicy = BMK_TRANSIT_TIME_FIRST;
        } else if ([policy isEqualToString:@"ebus_transfer_first"]) {
            transPlicy = BMK_TRANSIT_TRANSFER_FIRST;
        } else if ([policy isEqualToString:@"ebus_walk_first"]) {
            transPlicy = BMK_TRANSIT_WALK_FIRST;
        } else {
            transPlicy = BMK_TRANSIT_TIME_FIRST;
        }
    }
    switch (routeTypeValue) {
        case 0: {//开车
            BMKDrivingRoutePlanOption *driveInfo = [[BMKDrivingRoutePlanOption alloc]init];
            driveInfo.from = start;
            driveInfo.to  = end;
            driveInfo.drivingPolicy = drivePolicy;
            isSecceed = [routeSearcher drivingSearch:driveInfo];
        }
            break;
            
        case 1: {//公交
            if (!_geoSearch) {
                _geoSearch = [[UZBMKGeoCodeSearch alloc]init];
                _geoSearch.delegate=self;
            }
            _geoSearch.type = SEARCH_ROUTE;
            NSMutableDictionary *transitInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            [transitInfo setObject:start forKey:@"start"];
            [transitInfo setObject:end forKey:@"end"];
            [transitInfo setObject:[NSNumber numberWithInteger:transPlicy] forKey:@"transPlicy"];
            [transitInfo setObject:routeId forKey:@"routeId"];
            _geoSearch.transitInfo = transitInfo;
            BMKReverseGeoCodeOption *locInfo = [[BMKReverseGeoCodeOption alloc]init];
            locInfo.reverseGeoPoint = startCoor;
            isSecceed = [self.geoSearch reverseGeoCode:locInfo];
        }
            break;
            
        case 2: {//步行
            BMKWalkingRoutePlanOption *walkInfo = [[BMKWalkingRoutePlanOption alloc]init];
            walkInfo.from = start;
            walkInfo.to = end;
            isSecceed = [routeSearcher walkingSearch:walkInfo];
        }
            break;
            
        default:{//步行
            BMKWalkingRoutePlanOption *walkInfo = [[BMKWalkingRoutePlanOption alloc]init];
            walkInfo.from = start;
            walkInfo.to = end;
            isSecceed = [routeSearcher walkingSearch:walkInfo];
        }
            break;
    }
    if (!isSecceed) {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
        [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
        [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
    }
}

- (void)drawRoute:(NSDictionary *)paramsDict_ {
    NSString *routeId = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (routeId.length == 0) {
        return;
    }
    {//若路线已经添加，则移除先
        id target = [self.allRoutes objectForKey:routeId];
        if (target) {
            [self.baiduMapView removeOverlay:target];
        }
        NSArray *allNodes = [self.routeNodeSet objectForKey:routeId];
        [self.baiduMapView removeAnnotations:allNodes];
    }
    BOOL isAutoFit = [paramsDict_ boolValueForKey:@"autoresizing" defaultValue:YES];
    drawRouteCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSInteger planIndex = [paramsDict_ integerValueForKey:@"index" defaultValue:0];
    //起点终点图标
    NSDictionary *styleInfo = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    NSDictionary *styleStart = [styleInfo dictValueForKey:@"start" defaultValue:@{}];
    NSString *startIcon = [styleStart stringValueForKey:@"icon" defaultValue:nil];
    NSDictionary *styleEnd = [styleInfo dictValueForKey:@"end" defaultValue:@{}];
    NSString *endIcon = [styleEnd stringValueForKey:@"icon" defaultValue:nil];
    //添加路线
    NSArray *allPlans = [self.plans objectForKey:routeId];
    id routePlan = [allPlans objectAtIndex:planIndex];
    if ([routePlan isKindOfClass:[BMKWalkingRouteLine class]]) {
        [self addWalkingRoute:(BMKWalkingRouteLine *)routePlan withStartIcon:startIcon andEndIcon:endIcon idStr:routeId fit:isAutoFit];
    } else if ([routePlan isKindOfClass:[BMKDrivingRouteLine class]]) {
        [self addDriveRoute:(BMKDrivingRouteLine *)routePlan withStartIcon:startIcon andEndIcon:endIcon idStr:routeId fit:isAutoFit];
    }  else {
        [self addTransitRoute:(BMKTransitRouteLine *)routePlan withStartIcon:startIcon andEndIcon:endIcon idStr:routeId fit:isAutoFit];
    }
}

- (void)removeRoute:(NSDictionary *)paramsDict_ {
    NSArray *idAry = [paramsDict_ arrayValueForKey:@"ids" defaultValue:nil];
    if (idAry.count == 0) {
        return;
    }
    for (id idstr in idAry) {
        NSString *idString = [NSString stringWithFormat:@"%@",idstr];
        id target = [self.allRoutes objectForKey:idString];
        if (target) {
            [self.baiduMapView removeOverlay:target];
        }
        //移除标注
        NSArray *allNodes = [self.routeNodeSet objectForKey:idString];
        [self.baiduMapView removeAnnotations:allNodes];
    }
}

- (void)searchBusRoute:(NSDictionary *)paramsDict_ {
    NSString *city = [paramsDict_ stringValueForKey:@"city" defaultValue:nil];
    if (![city isKindOfClass:[NSString class]] || city.length==0) {
        return;
    }
    NSString *line = [paramsDict_ stringValueForKey:@"line" defaultValue:nil];
    if (![line isKindOfClass:[NSString class]] || line.length==0) {
        return;
    }
    int pageIndex = [paramsDict_ intValueForKey:@"pageIndex" defaultValue:0];
    int pageCapacity = [paramsDict_ intValueForKey:@"pageCapacity" defaultValue:10];
    getBusRouteCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    BMKCitySearchOption *citySearchOption = [[BMKCitySearchOption alloc]init];
    citySearchOption.pageIndex = pageIndex;
    citySearchOption.pageCapacity = pageCapacity;
    citySearchOption.city= city;
    citySearchOption.keyword = line;
    if (!_poisearch) {
        _poisearch = [[UZBMKPoiSearch alloc]init];
        _poisearch.delegate = self;
    }
    _poisearch.type = UZSEARCH_BUS;
    _poisearch.city = city;
    BOOL flag = [_poisearch poiSearchInCity:citySearchOption];
    if(!flag) {
        [self sendResultEventWithCallbackId:getBusRouteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:nil doDelete:YES];
    }
}

- (void)drawBusRoute:(NSDictionary *)paramsDict_ {
    NSString *routeId = [paramsDict_ stringValueForKey:@"id" defaultValue:nil];
    if (routeId.length == 0) {
        return;
    }
    {//若存在路线，则移除先
        id target = [self.allBusRoutes objectForKey:routeId];
        if (target) {
            [self.baiduMapView removeOverlay:target];
        }
        //移除标注
        NSArray *allNode = [self.allBusNodeSet objectForKey:routeId];
        if (allNode.count > 0) {
            [self.baiduMapView removeAnnotations:allNode];
        }

    }
    NSString *city = [paramsDict_ stringValueForKey:@"city" defaultValue:nil];
    if (![city isKindOfClass:[NSString class]] || city.length==0) {
        return;
    }
    NSString *uid = [paramsDict_ stringValueForKey:@"uid" defaultValue:nil];
    if (![uid isKindOfClass:[NSString class]] || uid.length==0) {
        return;
    }
    BOOL isAutoFit = [paramsDict_ boolValueForKey:@"autoresizing" defaultValue:YES];
    BOOL buslineNodeShow = [paramsDict_ boolValueForKey:@"nodeShow" defaultValue:YES];
    drawBusRouteCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    BMKBusLineSearchOption *buslineSearchOption = [[BMKBusLineSearchOption alloc]init];
    buslineSearchOption.city= city;
    buslineSearchOption.busLineUid= uid;
//    if (!_buslinesearch) {
//        _buslinesearch = [[UZBMKBusLineSearch alloc]init];
//        _buslinesearch.delegate = self;
//    }
    UZBMKBusLineSearch *_buslinesearch = [[UZBMKBusLineSearch alloc]init];
    _buslinesearch.delegate = self;
    _buslinesearch.autoFitVisible = isAutoFit;
    _buslinesearch.routId = routeId;
    _buslinesearch.showNode = buslineNodeShow;
    BOOL flag = [_buslinesearch busLineSearch:buslineSearchOption];
    if(!flag) {
        [self sendResultEventWithCallbackId:drawBusRouteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:nil doDelete:YES];
    }
}

- (void)removeBusRoute:(NSDictionary *)paramsDict_ {
    NSArray *idAry = [paramsDict_ arrayValueForKey:@"ids" defaultValue:nil];
    if (idAry.count == 0) {
        return;
    }
    for (id idstr in idAry) {
        NSString *idString = [NSString stringWithFormat:@"%@",idstr];
        id target = [self.allBusRoutes objectForKey:idString];
        if (target) {
            [self.baiduMapView removeOverlay:target];
        }
        //移除标注
        NSArray *allNode = [self.allBusNodeSet objectForKey:idString];
        if (allNode.count > 0) {
            [self.baiduMapView removeAnnotations:allNode];
        }
    }
}

- (void)searchInCity:(NSDictionary *)paramsDict_ {
    NSString *city = [paramsDict_ stringValueForKey:@"city" defaultValue:nil];
    if (![city isKindOfClass:[NSString class]] || city.length==0) {
        return;
    }
    NSString *keyword = [paramsDict_ stringValueForKey:@"keyword" defaultValue:nil];
    if (![keyword isKindOfClass:[NSString class]] || keyword.length==0) {
        return;
    }
    int pageIndex = [paramsDict_ intValueForKey:@"pageIndex" defaultValue:0];
    int pageCapacity = [paramsDict_ intValueForKey:@"pageCapacity" defaultValue:10];
    searchIncityCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    BMKCitySearchOption *citySearchOption = [[BMKCitySearchOption alloc]init];
    citySearchOption.pageIndex = pageIndex;
    citySearchOption.pageCapacity = pageCapacity;
    citySearchOption.city= city;
    citySearchOption.keyword = keyword;
    if (!_poisearch) {
        _poisearch = [[UZBMKPoiSearch alloc]init];
        _poisearch.delegate = self;
    }
    _poisearch.type = UZSEARCH_INCITY;
    BOOL flag = [_poisearch poiSearchInCity:citySearchOption];
    if(!flag){
        [self sendResultEventWithCallbackId:searchIncityCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:nil doDelete:YES];
    }
}

- (void)searchNearby:(NSDictionary *)paramsDict_ {
    NSString *keyword = [paramsDict_ stringValueForKey:@"keyword" defaultValue:nil];
    if (![keyword isKindOfClass:[NSString class]] || keyword.length==0) {
        return;
    }
    float radius = [paramsDict_ floatValueForKey:@"radius" defaultValue:0];
    if (radius == 0) {
        return;
    }
    float lon = [paramsDict_ floatValueForKey:@"lon" defaultValue:360];
    float lat = [paramsDict_ floatValueForKey:@"lat" defaultValue:360];
    if (![self isValidLon:lon lat:lat]) {
        return;
    }
    CLLocationCoordinate2D coordinate;
    coordinate.latitude = lat;
    coordinate.longitude = lon;
    int pgIndex = [paramsDict_ intValueForKey:@"pageIndex" defaultValue:0];
    int pageCapacity = [paramsDict_ intValueForKey:@"pageCapacity" defaultValue:10];
    searchNearByCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    if (!_poisearch) {
        _poisearch = [[UZBMKPoiSearch alloc]init];
        _poisearch.delegate = self;
    }
    _poisearch.type = UZSEARCH_NEARBY;
    BMKNearbySearchOption *nearInfo = [[BMKNearbySearchOption alloc]init];
    nearInfo.location = coordinate;
    nearInfo.radius = radius;
    nearInfo.keyword = keyword;
    nearInfo.pageIndex = pgIndex;
    nearInfo.pageCapacity = pageCapacity;
    BOOL isSearch = [_poisearch poiSearchNearBy:nearInfo];
    if (!isSearch) {
        [self sendResultEventWithCallbackId:searchNearByCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
    }
}

- (void)searchInBounds:(NSDictionary *)paramsDict_ {
    NSString *keyword = [paramsDict_ stringValueForKey:@"keyword" defaultValue:nil];
    if (![keyword isKindOfClass:[NSString class]] || keyword.length==0) {
        return;
    }
    float lbLon = [paramsDict_ floatValueForKey:@"lbLon" defaultValue:360];
    float lbLat = [paramsDict_ floatValueForKey:@"lbLat" defaultValue:360];
    if (![self isValidLon:lbLon lat:lbLat]) {
        return;
    }
    float rtLon = [paramsDict_ floatValueForKey:@"rtLon" defaultValue:360];
    float rtLat = [paramsDict_ floatValueForKey:@"rtLat" defaultValue:360];
    if (![self isValidLon:rtLon lat:rtLat]) {
        return;
    }
    CLLocationCoordinate2D ltCoord;
    ltCoord.longitude = lbLon;
    ltCoord.latitude = lbLat;
    CLLocationCoordinate2D rbCoord;
    rbCoord.longitude = rtLon;
    rbCoord.latitude = rtLat;
    int pgIndex = [paramsDict_ intValueForKey:@"pageIndex" defaultValue:0];
    int pageCapacity = [paramsDict_ intValueForKey:@"pageCapacity" defaultValue:10];
    searchInboundsCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    if (!_poisearch) {
        _poisearch = [[UZBMKPoiSearch alloc]init];
        _poisearch.delegate = self;
    }
    _poisearch.type = UZSEARCH_INBOUNDS;
    BMKBoundSearchOption *nearInfo = [[BMKBoundSearchOption alloc]init];
    nearInfo.leftBottom = ltCoord;
    nearInfo.rightTop = rbCoord;
    nearInfo.keyword = keyword;
    nearInfo.pageIndex = pgIndex;
    nearInfo.pageCapacity = pageCapacity;
    BOOL isSearch = [_poisearch poiSearchInbounds:nearInfo];
    if (!isSearch) {
        [self sendResultEventWithCallbackId:searchInboundsCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
    }
}

- (void)autocomplete:(NSDictionary *)paramsDict_ {
    NSString *keyword = [paramsDict_ stringValueForKey:@"keyword" defaultValue:nil];
    if (![keyword isKindOfClass:[NSString class]] || keyword.length==0) {
        return;
    }
    NSString *city = [paramsDict_ stringValueForKey:@"city" defaultValue:nil];
    if (![city isKindOfClass:[NSString class]] || city.length==0) {
        return;
    }
    autoCompleteCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    BMKSuggestionSearch *suggestSearch = [[BMKSuggestionSearch alloc]init];
    suggestSearch.delegate = self;
    BMKSuggestionSearchOption *suggestionSearch = [[BMKSuggestionSearchOption alloc]init];
    suggestionSearch.keyword = keyword;
    suggestionSearch.cityname = city;
    BOOL isSearch = [suggestSearch suggestionSearch:suggestionSearch];
    if (!isSearch) {
        [self sendResultEventWithCallbackId:autoCompleteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:@"搜索失败" forKey:@"msg"] doDelete:YES];
    }
}

#pragma mark -
#pragma mark 离线地图类接口
#pragma mark -

- (void)getHotCityList:(NSDictionary *)paramsDict_ {
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    NSArray *hotCityList = [_offlineMap getHotCityList];
    if (hotCityList.count > 0) {
        NSMutableArray *hotCitysAry = [NSMutableArray array];
        for (BMKOLSearchRecord *hotCity in hotCityList) {
            NSMutableDictionary *cityInfo = [NSMutableDictionary dictionary];
            NSString *name = hotCity.cityName;
            if ([name isKindOfClass:[NSString class]] && name.length>0) {
                [cityInfo setObject:name forKey:@"name"];
            }
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.size] forKey:@"size"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityID] forKey:@"cityID"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityType] forKey:@"cityType"];
            NSArray *childCity = hotCity.childCities;
            if (childCity.count > 0) {
                NSMutableArray *childCityAry = [NSMutableArray array];
                for (BMKOLSearchRecord *childhotCity in childCity) {
                    NSMutableDictionary *childCityInfo = [NSMutableDictionary dictionary];
                    NSString *name = childhotCity.cityName;
                    if ([name isKindOfClass:[NSString class]] && name.length>0) {
                        [childCityInfo setObject:name forKey:@"name"];
                    }
                    [childCityInfo setObject:[NSNumber numberWithInt:childhotCity.cityID] forKey:@"cityID"];
                    [childCityAry addObject:childCityInfo];
                }
                [cityInfo setObject:childCityAry forKey:@"childCities"];
            }
            [hotCitysAry addObject:cityInfo];
        }
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:hotCitysAry forKey:@"records"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    } else {
        [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    }
}

- (void)getOfflineCityList:(NSDictionary *)paramsDict_ {
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    NSArray *hotCityList = [_offlineMap getOfflineCityList];
    if (hotCityList.count > 0) {
        NSMutableArray *hotCitysAry = [NSMutableArray array];
        for (BMKOLSearchRecord *hotCity in hotCityList) {
            NSMutableDictionary *cityInfo = [NSMutableDictionary dictionary];
            NSString *name = hotCity.cityName;
            if ([name isKindOfClass:[NSString class]] && name.length>0) {
                [cityInfo setObject:name forKey:@"name"];
            }
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.size] forKey:@"size"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityID] forKey:@"cityID"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityType] forKey:@"cityType"];
            NSArray *childCity = hotCity.childCities;
            if (childCity.count > 0) {
                NSMutableArray *childCityAry = [NSMutableArray array];
                for (BMKOLSearchRecord *childhotCity in childCity) {
                    NSMutableDictionary *childCityInfo = [NSMutableDictionary dictionary];
                    NSString *name = childhotCity.cityName;
                    if ([name isKindOfClass:[NSString class]] && name.length>0) {
                        [childCityInfo setObject:name forKey:@"name"];
                    }
                    [childCityInfo setObject:[NSNumber numberWithInt:childhotCity.cityID] forKey:@"cityID"];
                    [childCityAry addObject:childCityInfo];
                }
                [cityInfo setObject:childCityAry forKey:@"childCities"];
            }
            [hotCitysAry addObject:cityInfo];
        }
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:hotCitysAry forKey:@"records"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    } else {
        [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    }
}

- (void)searchCityByName:(NSDictionary *)paramsDict_ {
    NSString *cityName = [paramsDict_ stringValueForKey:@"name" defaultValue:nil];
    if (cityName.length == 0) {
        return;
    }
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    NSArray *hotCityList = [_offlineMap searchCity:cityName];
    if (hotCityList.count > 0) {
        NSMutableArray *hotCitysAry = [NSMutableArray array];
        for (BMKOLSearchRecord *hotCity in hotCityList) {
            NSMutableDictionary *cityInfo = [NSMutableDictionary dictionary];
            NSString *name = hotCity.cityName;
            if ([name isKindOfClass:[NSString class]] && name.length>0) {
                [cityInfo setObject:name forKey:@"name"];
            }
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.size] forKey:@"size"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityID] forKey:@"cityID"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityType] forKey:@"cityType"];
            NSArray *childCity = hotCity.childCities;
            if (childCity.count > 0) {
                NSMutableArray *childCityAry = [NSMutableArray array];
                for (BMKOLSearchRecord *childhotCity in childCity) {
                    NSMutableDictionary *childCityInfo = [NSMutableDictionary dictionary];
                    NSString *name = childhotCity.cityName;
                    if ([name isKindOfClass:[NSString class]] && name.length>0) {
                        [childCityInfo setObject:name forKey:@"name"];
                    }
                    [childCityInfo setObject:[NSNumber numberWithInt:childhotCity.cityID] forKey:@"cityID"];
                    [childCityAry addObject:childCityInfo];
                }
                [cityInfo setObject:childCityAry forKey:@"childCities"];
            }
            [hotCitysAry addObject:cityInfo];
        }
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:hotCitysAry forKey:@"records"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    } else {
        [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    }
}

- (void)getAllUpdateInfo:(NSDictionary *)paramsDict_ {
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    NSArray *hotCityList = [_offlineMap getAllUpdateInfo];
    if (hotCityList.count > 0) {
        NSMutableArray *hotCitysAry = [NSMutableArray array];
        for (BMKOLUpdateElement *hotCity in hotCityList) {
            NSMutableDictionary *cityInfo = [NSMutableDictionary dictionary];
            NSString *name = hotCity.cityName;
            if ([name isKindOfClass:[NSString class]] && name.length>0) {
                [cityInfo setObject:name forKey:@"name"];
            }
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.size] forKey:@"size"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityID] forKey:@"cityID"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.ratio] forKey:@"ratio"];
            [cityInfo setObject:[NSNumber numberWithInt:hotCity.status] forKey:@"status"];
            [cityInfo setObject:[NSNumber numberWithBool:hotCity.update] forKey:@"update"];
            float lat = hotCity.pt.latitude;
            float lon = hotCity.pt.longitude;
            [cityInfo setObject:[NSNumber numberWithFloat:lat] forKey:@"lat"];
            [cityInfo setObject:[NSNumber numberWithFloat:lon] forKey:@"lon"];
            [hotCitysAry addObject:cityInfo];
        }
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:hotCitysAry forKey:@"records"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    } else {
        [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    }
}

- (void)getUpdateInfoByID:(NSDictionary *)paramsDict_ {
    int cityID = [paramsDict_ intValueForKey:@"cityID" defaultValue:-1];
    if (cityID < 0) {
        return;
    }
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    BMKOLUpdateElement *hotCity = [_offlineMap getUpdateInfo:cityID];
    if (hotCity) {
        NSMutableDictionary *cityInfo = [NSMutableDictionary dictionary];
        NSString *name = hotCity.cityName;
        if ([name isKindOfClass:[NSString class]] && name.length>0) {
            [cityInfo setObject:name forKey:@"name"];
        }
        [cityInfo setObject:[NSNumber numberWithInt:hotCity.size] forKey:@"size"];
        [cityInfo setObject:[NSNumber numberWithInt:hotCity.cityID] forKey:@"cityID"];
        [cityInfo setObject:[NSNumber numberWithInt:hotCity.ratio] forKey:@"ratio"];
        [cityInfo setObject:[NSNumber numberWithInt:hotCity.status] forKey:@"status"];
        [cityInfo setObject:[NSNumber numberWithBool:hotCity.update] forKey:@"update"];
        float lat = hotCity.pt.latitude;
        float lon = hotCity.pt.longitude;
        [cityInfo setObject:[NSNumber numberWithFloat:lat] forKey:@"lat"];
        [cityInfo setObject:[NSNumber numberWithFloat:lon] forKey:@"lon"];
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:cityInfo forKey:@"cityInfo"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    } else {
        [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
        [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
    }
}

- (void)start:(NSDictionary *)paramsDict_ {
    int cityID = [paramsDict_ intValueForKey:@"cityID" defaultValue:-1];
    if (cityID < 0) {
        return;
    }
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    BOOL success = [_offlineMap start:cityID];
    [sendDict setObject:[NSNumber numberWithBool:success] forKey:@"status"];
    [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)update:(NSDictionary *)paramsDict_ {
    int cityID = [paramsDict_ intValueForKey:@"cityID" defaultValue:-1];
    if (cityID < 0) {
        return;
    }
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    BOOL success = [_offlineMap update:cityID];
    [sendDict setObject:[NSNumber numberWithBool:success] forKey:@"status"];
    [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)pause:(NSDictionary *)paramsDict_ {
    int cityID = [paramsDict_ intValueForKey:@"cityID" defaultValue:-1];
    if (cityID < 0) {
        return;
    }
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    BOOL success = [_offlineMap pause:cityID];
    [sendDict setObject:[NSNumber numberWithBool:success] forKey:@"status"];
    [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)remove:(NSDictionary *)paramsDict_ {
    int cityID = [paramsDict_ intValueForKey:@"cityID" defaultValue:-1];
    if (cityID < 0) {
        return;
    }
    NSInteger getCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    if (!_offlineMap) {
        [self initOfflineMap];
    }
    BOOL success = [_offlineMap remove:cityID];
    [sendDict setObject:[NSNumber numberWithBool:success] forKey:@"status"];
    [self sendResultEventWithCallbackId:getCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)addOfflineListener:(NSDictionary *)paramsDict_ {
    offlineListenerCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
}

- (void)removeOfflineListener:(NSDictionary *)paramsDict_ {
    offlineListenerCbid = -1;
}

- (void)onGetOfflineMapState:(int)type withState:(int)state {
    if (offlineListenerCbid >= 0) {
        [self sendResultEventWithCallbackId:offlineListenerCbid dataDict:[NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithInt:type],@"type",[NSNumber numberWithInt:state],@"state", nil] errDict:nil doDelete:NO];
    }
}

#pragma mark -
#pragma mark 建议搜索代理
#pragma mark -

- (void)didMoving:(UZbMapAnnotation *)anno {
    anno.delegate = nil;
    anno.timeOffset = 0;
    NSString *moveId= [NSString stringWithFormat:@"%zi",anno.annoId];
    [_allMovingAnno removeObjectForKey:moveId];
    [self sendResultEventWithCallbackId:moveAnnoCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInteger:anno.annoId] forKey:@"id"] errDict:nil doDelete:NO];
}

#pragma mark -
#pragma mark 建议搜索代理
#pragma mark -

- (void)onGetSuggestionResult:(BMKSuggestionSearch *)searcher result:(BMKSuggestionResult *)result errorCode:(BMKSearchErrorCode)error {
    if (error == BMK_SEARCH_NO_ERROR) {
        NSArray *keylist = result.keyList;
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:keylist forKey:@"results"];
        [self sendResultEventWithCallbackId:autoCompleteCbid dataDict:sendDict errDict:nil doDelete:YES];
    } else {
        int errCode = 0;
        switch (error) {
            case BMK_SEARCH_AMBIGUOUS_KEYWORD:
                errCode = 1;
                break;
                
            case BMK_SEARCH_AMBIGUOUS_ROURE_ADDR:
                errCode = 2;
                break;
                
            case BMK_SEARCH_RESULT_NOT_FOUND:
                errCode = 3;
                break;
                
            case BMK_SEARCH_KEY_ERROR:
                errCode = 4;
                break;
                
            case BMK_SEARCH_NETWOKR_ERROR:
                errCode = 5;
                break;
                
            case BMK_SEARCH_NETWOKR_TIMEOUT:
                errCode = 6;
                break;
                
            case BMK_SEARCH_PERMISSION_UNFINISHED:
                errCode = 7;
                break;
                
            default:
                break;
        }
        [self sendResultEventWithCallbackId:autoCompleteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:errCode] forKey:@"code"]  doDelete:YES];
    }
}

#pragma mark -
#pragma mark poi搜索代理
#pragma mark -

- (void)onGetPoiResult:(BMKPoiSearch *)searcher result:(BMKPoiResult *)poiResult errorCode:(BMKSearchErrorCode)errorCode {
    UZBMKPoiSearch *tempSearcher = (UZBMKPoiSearch *)searcher;
    if (errorCode == BMK_SEARCH_NO_ERROR) {
        switch (tempSearcher.type) {
                
            case UZSEARCH_BUS:
                [self searchBusRoute:tempSearcher withResult:poiResult andErroCode:errorCode];
                break;
                
            case UZSEARCH_INCITY:
            case UZSEARCH_NEARBY:
            case UZSEARCH_INBOUNDS:
                [self searchInCityRoute:tempSearcher withResult:poiResult andErroCode:errorCode];
                break;
                
            default:
                [self searchBusRoute:tempSearcher withResult:poiResult andErroCode:errorCode];
                break;
        }
    } else {
        NSInteger cbidsSearch = -1;
        switch (tempSearcher.type) {
                
            case UZSEARCH_INCITY:
                cbidsSearch = searchIncityCbid;
                break;
                
            case UZSEARCH_NEARBY:
                cbidsSearch = searchNearByCbid;
                break;
                
            case UZSEARCH_INBOUNDS:
                cbidsSearch = searchInboundsCbid;
                break;
                
            default:
                cbidsSearch = searchIncityCbid;
                break;
        }
        int errCode = 0;
        switch (errorCode) {
            case BMK_SEARCH_AMBIGUOUS_KEYWORD:
                errCode = 1;
                break;
                
            case BMK_SEARCH_AMBIGUOUS_ROURE_ADDR:
                errCode = 2;
                break;
                
            case BMK_SEARCH_RESULT_NOT_FOUND:
                errCode = 3;
                break;
                
            case BMK_SEARCH_KEY_ERROR:
                errCode = 4;
                break;
                
            case BMK_SEARCH_NETWOKR_ERROR:
                errCode = 5;
                break;
                
            case BMK_SEARCH_NETWOKR_TIMEOUT:
                errCode = 6;
                break;
                
            case BMK_SEARCH_PERMISSION_UNFINISHED:
                errCode = 7;
                break;
                
            default:
                break;
        }
        [self sendResultEventWithCallbackId:cbidsSearch dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:errCode] forKey:@"code"]  doDelete:YES];
    }
}

#pragma mark -
#pragma mark 公交搜索代理
#pragma mark -

- (void)onGetBusDetailResult:(BMKBusLineSearch *)searcher result:(BMKBusLineResult *)busLineResult errorCode:(BMKSearchErrorCode)error {
    if (error == BMK_SEARCH_NO_ERROR) {
        UZBMKBusLineSearch *tempSearcher = (UZBMKBusLineSearch *)searcher;
        NSString *routeId = tempSearcher.routId;
        //站点信息
        NSInteger size = 0;
        size = busLineResult.busStations.count;
        NSMutableArray *stationAry = [NSMutableArray arrayWithCapacity:10];
        NSMutableArray *stationNode = [NSMutableArray arrayWithCapacity:10];
        for (int j = 0; j < size; j++) {
            NSMutableDictionary *stationDict = [NSMutableDictionary dictionaryWithCapacity:4];
            BMKBusStation *station = [busLineResult.busStations objectAtIndex:j];
            if (tempSearcher.showNode) {
                UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
                item.coordinate = station.location;
                item.title = station.title;
                item.classify = ANNOTATION_ROUTE_BUS;
                item.clikType = ANNOTATION_CLICK_BUS_ROUTE;
                item.nodeIndex = j;
                item.routeLineId = routeId;
                [_baiduMapView addAnnotation:item];
                [stationNode addObject:item];
            }
            NSString *description = station.title;
            if (description.length > 0) {
                [stationDict setObject:description forKey:@"description"];
            }
            double lon = station.location.longitude;
            double lat = station.location.latitude;
            [stationDict setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
            [stationDict setObject:[NSNumber numberWithDouble:lat] forKey:@"lat"];
            [stationAry addObject:stationDict];
        }
        //路段信息
        int index = 0;
        //累加index为下面声明数组temppoints时用
        for (int j = 0; j < busLineResult.busSteps.count; j++) {
            BMKBusStep *step = [busLineResult.busSteps objectAtIndex:j];
            index += step.pointsCount;
        }
        //直角坐标划线
        BMKMapPoint *temppoints = new BMKMapPoint[index];
        int k=0;
        for (int i = 0; i < busLineResult.busSteps.count; i++) {
            BMKBusStep *step = [busLineResult.busSteps objectAtIndex:i];
            for (int j = 0; j < step.pointsCount; j++) {
                BMKMapPoint pointarray;
                pointarray.x = step.points[j].x;
                pointarray.y = step.points[j].y;
                temppoints[k] = pointarray;
                k++;
            }
        }
        //在地图上添加公交路线
        UZBMKPolyline *polyLine = [[UZBMKPolyline alloc]init];
        [polyLine setPolylineWithPoints:temppoints count:index];
        polyLine.lineType = 1;
        [_baiduMapView addOverlay:polyLine];
        if (routeId.length >0 ) {
            [_allBusRoutes setObject:polyLine forKey:routeId];
            [_allBusNodeSet setObject:stationNode forKey:routeId];
        }
        delete [] temppoints;
        //移动地图到合适的位置
        if (tempSearcher.autoFitVisible) {
            [self mapViewFitPolyLine:polyLine];
            //BMKBusStation *start = [busLineResult.busStations objectAtIndex:0];
            //[_baiduMapView setCenterCoordinate:start.location animated:YES];
        }
        //回调
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:4];
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        NSString *name = busLineResult.busLineName;
        if (name && name.length>0) {
            [sendDict setObject:name forKey:@"name"];
        }
        NSString *company = busLineResult.busCompany;
        if (company && company.length>0) {
            [sendDict setObject:company forKey:@"company"];
        }
        NSString *startTime = busLineResult.startTime;
        if (startTime && startTime.length>0) {
            [sendDict setObject:startTime forKey:@"startTime"];
        }
        NSString *endTime = busLineResult.endTime;
        if (endTime && endTime.length>0) {
            [sendDict setObject:endTime forKey:@"endTime"];
        }
        if (stationAry.count > 0) {
            [sendDict setObject:stationAry forKey:@"stations"];
        }
        [sendDict setObject:[NSNumber numberWithBool:busLineResult.isMonTicket] forKey:@"isMonTicket"];
        [sendDict setObject:@"draw" forKey:@"eventType"];
        [self sendResultEventWithCallbackId:drawBusRouteCbid dataDict:sendDict errDict:nil doDelete:NO];
    } else {
        int errCode = 0;
        switch (error) {
            case BMK_SEARCH_AMBIGUOUS_KEYWORD:
                errCode = 1;
                break;
                
            case BMK_SEARCH_AMBIGUOUS_ROURE_ADDR:
                errCode = 2;
                break;
                
            case BMK_SEARCH_RESULT_NOT_FOUND:
                errCode = 3;
                break;
                
            case BMK_SEARCH_KEY_ERROR:
                errCode = 4;
                break;
                
            case BMK_SEARCH_NETWOKR_ERROR:
                errCode = 5;
                break;
                
            case BMK_SEARCH_NETWOKR_TIMEOUT:
                errCode = 6;
                break;
                
            case BMK_SEARCH_PERMISSION_UNFINISHED:
                errCode = 7;
                break;
                
            default:
                break;
        }
        [self sendResultEventWithCallbackId:drawBusRouteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:errCode] forKey:@"code"]  doDelete:YES];
    }
}

#pragma mark -
#pragma mark 百度地图基础代理函数
#pragma mark -

 - (BMKOverlayView *)mapView:(BMKMapView *)mapView viewForOverlay:(id <BMKOverlay>)overlay {//地图添加覆盖物代理
     if ([overlay isKindOfClass:[BMKPolyline class]]) {
         UZBMKPolyline *temp = (UZBMKPolyline *)overlay;
         if (temp.lineType != 0) {
             BMKPolylineView *polylineView = [[BMKPolylineView alloc] initWithOverlay:overlay];
             polylineView.fillColor = [[UIColor cyanColor] colorWithAlphaComponent:1];
             polylineView.strokeColor = [[UIColor blueColor] colorWithAlphaComponent:0.8];
             polylineView.lineWidth = 3.0;
             return polylineView;
         }
         BMKPolylineView *polylineView = [[BMKPolylineView alloc] initWithOverlay:overlay];
         NSString *color =[self.overlayStyles stringValueForKey:@"borderColor" defaultValue:@"#000"];
         float width = [self.overlayStyles floatValueForKey:@"borderWidth" defaultValue:2];
         polylineView.lineWidth = width;
         polylineView.lineDash = [self.overlayStyles boolValueForKey:@"lineDash" defaultValue:NO];
         NSString *dashImg = [self.overlayStyles stringValueForKey:@"dashImg" defaultValue:nil];
         if ([dashImg isKindOfClass:[NSString class]] && dashImg.length>0) {
             UIImage *dashImage = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:dashImg]];
             [polylineView loadStrokeTextureImage:dashImage];
         } else {
             polylineView.strokeColor = [UZAppUtils colorFromNSString:color];
         }
         return polylineView;
     }
     if ([overlay isKindOfClass:[BMKPolygon class]]) {
         BMKPolygonView *polygonView = [[BMKPolygonView alloc] initWithOverlay:overlay];
         NSString *borColor = [self.overlayStyles stringValueForKey:@"borderColor" defaultValue:@"#000"];
         NSString *filColor = [self.overlayStyles stringValueForKey:@"fillColor" defaultValue:@"#000"];
         polygonView.strokeColor = [UZAppUtils colorFromNSString:borColor];
         polygonView.fillColor = [UZAppUtils colorFromNSString:filColor];
         polygonView.lineWidth = [self.overlayStyles floatValueForKey:@"borderWidth" defaultValue:2];
         polygonView.lineDash = [self.overlayStyles boolValueForKey:@"lineDash" defaultValue:NO];
         return polygonView;
     }
     if ([overlay isKindOfClass:[BMKCircle class]]) {
         BMKCircleView *circleView = [[BMKCircleView alloc] initWithOverlay:overlay];
         NSString *borColor = [self.overlayStyles stringValueForKey:@"borderColor" defaultValue:@"#000"];
         NSString *filColor = [self.overlayStyles stringValueForKey:@"fillColor" defaultValue:@"#000"];
         circleView.strokeColor = [UZAppUtils colorFromNSString:borColor];
         circleView.fillColor = [UZAppUtils colorFromNSString:filColor];
         circleView.lineWidth = [self.overlayStyles floatValueForKey:@"borderWidth" defaultValue:2];
         circleView.lineDash = [self.overlayStyles boolValueForKey:@"lineDash" defaultValue:NO];
         return circleView;
     }
     if ([overlay isKindOfClass:[BMKGroundOverlay class]]) {
         BMKGroundOverlayView* groundView = [[BMKGroundOverlayView alloc] initWithOverlay:overlay];
         return groundView;
     }
     if ([overlay isKindOfClass:[BMKArcline class]]) {
         BMKArclineView *arclineView = [[BMKArclineView alloc] initWithArcline:overlay];
         NSString *borColor = [self.overlayStyles stringValueForKey:@"borderColor" defaultValue:@"#000"];
         arclineView.strokeColor = [UZAppUtils colorFromNSString:borColor];
         arclineView.lineDash = [self.overlayStyles boolValueForKey:@"lineDash" defaultValue:NO];;
         arclineView.lineWidth = [self.overlayStyles floatValueForKey:@"borderWidth" defaultValue:2];
         return arclineView;
     }
     return nil;
}

- (BMKAnnotationView *)mapView:(BMKMapView *)mapView viewForAnnotation:(id <BMKAnnotation>)annotation {//地图添加标注代理
    UZbMapAnnotation *tempAnnot = (UZbMapAnnotation *) annotation;
    switch (tempAnnot.classify) {
            
        case ANNOTATION:
            return [self getDefaultAnnotaionWith:tempAnnot andMap:mapView];
            break;
            
        case ANNOTATION_ROUTE_START: {
              BMKPinAnnotationView *view = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:@"start_node"];
              if (view == nil) {
                  view = [[BMKPinAnnotationView alloc]initWithAnnotation:tempAnnot reuseIdentifier:@"start_node"];
                  UIImage *pinImg = nil;
                  if (tempAnnot.pinImg.length > 0) {
                      pinImg = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:tempAnnot.pinImg]];
                  } else {
                      pinImg = [UIImage imageWithContentsOfFile:[self getMyBundlePath:@"images/icon_nav_start.png"]];
                  }
                  view.image = pinImg;
                  view.centerOffset = CGPointMake(0, -(view.frame.size.height * 0.5));
                  view.animatesDrop = NO;
              }
              return view;
            }
            break;
            
        case ANNOTATION_ROUTE_END: {
            BMKPinAnnotationView *view = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:@"end_node"];
            if (view == nil) {
                view = [[BMKPinAnnotationView alloc]initWithAnnotation:tempAnnot reuseIdentifier:@"end_node"];
                UIImage *pinImg = nil;
                if (tempAnnot.pinImg.length > 0) {
                    pinImg = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:tempAnnot.pinImg]];
                } else {
                    pinImg = [UIImage imageWithContentsOfFile:[self getMyBundlePath:@"images/icon_nav_end.png"]];
                }
                view.image = pinImg;
                view.centerOffset = CGPointMake(0, -(view.frame.size.height * 0.5));
                view.animatesDrop = NO;
            }
            return view;
        }
            break;
            
        case ANNOTATION_ROUTE_NODE: {
                BMKPinAnnotationView *view = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:@"route_node"];
                if (view == nil) {
                    view = [[BMKPinAnnotationView alloc]initWithAnnotation:tempAnnot reuseIdentifier:@"route_node"];
                    UIImage *pinImg = nil;
                    if (tempAnnot.pinImg.length > 0) {
                        pinImg = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:tempAnnot.pinImg]];
                    } else {
                        pinImg = [UIImage imageWithContentsOfFile:[self getMyBundlePath:@"images/icon_direction.png"]];
                    }
                    if (tempAnnot.degree > 0) {
                        view.image = [self imageRotated:pinImg ByDegrees:tempAnnot.degree];
                    } else {
                        view.image = pinImg;
                    }
                    view.centerOffset = CGPointMake(0, -(view.frame.size.height * 0.5));
                    view.animatesDrop = NO;
                }
                return view;
            }
            break;
            
        case ANNOTATION_ROUTE_BUS: {
                BMKPinAnnotationView *view = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:@"bus_node"];
                if (view == nil) {
                    view = [[BMKPinAnnotationView alloc]initWithAnnotation:tempAnnot reuseIdentifier:@"bus_node"];
                    UIImage *pinImg = nil;
                    if (tempAnnot.pinImg.length > 0) {
                        pinImg = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:tempAnnot.pinImg]];
                    } else {
                        pinImg = [UIImage imageWithContentsOfFile:[self getMyBundlePath:@"images/icon_nav_bus.png"]];
                    }
                    if (tempAnnot.degree > 0) {
                        view.image = [self imageRotated:pinImg ByDegrees:tempAnnot.degree];
                    } else {
                        view.image = pinImg;
                    }
                    view.centerOffset = CGPointMake(0, -(view.frame.size.height * 0.5));
                    view.animatesDrop = NO;
                }
                return view;
            }
            break;
            
        case ANNOTATION_ROUTE_RAIL: {
                BMKPinAnnotationView *view = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:@"rail_node"];
                if (view == nil) {
                    view = [[BMKPinAnnotationView alloc]initWithAnnotation:tempAnnot reuseIdentifier:@"rail_node"];
                    UIImage *pinImg = nil;
                    if (tempAnnot.pinImg.length > 0) {
                        pinImg = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:tempAnnot.pinImg]];
                    } else {
                        pinImg = [UIImage imageWithContentsOfFile:[self getMyBundlePath:@"images/icon_nav_rail.png"]];
                    }
                    if (tempAnnot.degree > 0) {
                        view.image = [self imageRotated:pinImg ByDegrees:tempAnnot.degree];
                    } else {
                        view.image = pinImg;
                    }
                    view.centerOffset = CGPointMake(0, -(view.frame.size.height * 0.5));
                    view.animatesDrop = NO;
                }
                return view;
            }
            break;
            
        default:
            return [self getDefaultAnnotaionWith:tempAnnot andMap:mapView];
            break;
    }
}

- (void)mapView:(BMKMapView *)mapView annotationViewForBubble:(BMKAnnotationView *)view {//气泡点击代理
    if (setBubbleCbid >= 0) {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
        [sendDict setObject:[NSNumber numberWithInteger:[view.reuseIdentifier integerValue]] forKey:@"id"];
        [sendDict setObject:@"clickContent" forKey:@"eventType"];
        [self sendResultEventWithCallbackId:setBubbleCbid dataDict:sendDict errDict:nil doDelete:NO];
    }
}

- (void)mapView:(BMKMapView *)mapView annotationView:(BMKAnnotationView *)view didChangeDragState:(BMKAnnotationViewDragState)newState
   fromOldState:(BMKAnnotationViewDragState)oldState {//标注拖动状态改变代理
    UZbMapAnnotation *temp = (UZbMapAnnotation *)view.annotation;
    NSInteger addAnnCbid = temp.clickCbId;
    NSString *state = @"none";
    switch (newState) {
        case BMKAnnotationViewDragStateNone:
            state = @"none";
        case BMKAnnotationViewDragStateStarting:
            state = @"starting";
            break;
        case BMKAnnotationViewDragStateDragging:
            state = @"dragging";
            break;
        case BMKAnnotationViewDragStateCanceling:
            state = @"canceling";
            break;
        case BMKAnnotationViewDragStateEnding:
            state = @"ending";
            break;
            
        default:
            state = @"none";
            break;
    }
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
    [sendDict setObject:[NSNumber numberWithInteger:[view.reuseIdentifier integerValue]] forKey:@"id"];
    [sendDict setObject:@"drag" forKey:@"eventType"];
    [sendDict setObject:state forKeyedSubscript:@"dragState"];
    [self sendResultEventWithCallbackId:addAnnCbid dataDict:sendDict errDict:nil doDelete:NO];
}
- (void)mapView:(BMKMapView *)mapView didSelectAnnotationView:(BMKAnnotationView *)view {//标注点击代理
    UZbMapAnnotation *temp = (UZbMapAnnotation *)view.annotation;
    if ([temp respondsToSelector:@selector(setPopBubble:)]) {
        if (temp.popBubble) {
            temp.popBubble = NO;
            return;
        }
    } else {
        return;
    }
    NSInteger addAnnCbid = temp.clickCbId;
    switch (temp.clikType) {
        case ANNOTATION_CLICK: {
            if (addAnnCbid >= 0) {
                if (!temp.isStyled) {
                    [mapView deselectAnnotation:temp animated:NO];
                }
                NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
                [sendDict setObject:[NSNumber numberWithInteger:[view.reuseIdentifier integerValue]] forKey:@"id"];
                [sendDict setObject:@"click" forKey:@"eventType"];
                [self sendResultEventWithCallbackId:addAnnCbid dataDict:sendDict errDict:nil doDelete:NO];
            }
        }
            break;
            
        case ANNOTATION_CLICK_ROUTE: {
            if (drawRouteCbid >= 0) {
                [mapView deselectAnnotation:temp animated:NO];
                NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
                [sendDict setObject:[NSNumber numberWithInteger:temp.nodeIndex] forKey:@"nodeIndex"];
                NSString *routeid = temp.routeLineId;
                if (routeid.length > 0) {
                    [sendDict setObject:routeid forKey:@"routeId"];
                }
                [self sendResultEventWithCallbackId:drawRouteCbid dataDict:sendDict errDict:nil doDelete:NO];
            }
        }
            break;
            
        case ANNOTATION_CLICK_BUS_ROUTE: {
            if (drawBusRouteCbid >= 0) {
                [mapView deselectAnnotation:temp animated:NO];
                NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
                [sendDict setObject:[NSNumber numberWithInteger:temp.nodeIndex] forKey:@"nodeIndex"];
                NSString *routeid = temp.routeLineId;
                if (routeid.length > 0) {
                    [sendDict setObject:[NSNumber numberWithInt:[routeid intValue]] forKey:@"routeId"];
                }
                [sendDict setObject:@"click" forKey:@"eventType"];
                [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
                [self sendResultEventWithCallbackId:drawBusRouteCbid dataDict:sendDict errDict:nil doDelete:NO];
            }
        }
            
        case ANNOTATION_CLICK_POINT: {
            [mapView deselectAnnotation:temp animated:NO];
        }
            
        case ANNOTATION_CLICK_MOBILEANNO:
        default:
            break;
    }
}

- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated {//地图移动代理
    if (viewChangeCbid >= 0) {
        float lon = self.baiduMapView.region.center.longitude;
        float lat = self.baiduMapView.region.center.latitude;
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:3];
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:[NSNumber numberWithFloat:lon] forKey:@"lon"];
        [sendDict setObject:[NSNumber numberWithFloat:lat] forKey:@"lat"];
        [sendDict setObject:[NSNumber numberWithFloat:self.baiduMapView.zoomLevel] forKey:@"zoom"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.rotation] forKey:@"rotate"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.overlooking] forKey:@"overlook"];
        [self sendResultEventWithCallbackId:viewChangeCbid dataDict:sendDict errDict:nil doDelete:NO];
    }
}

- (void)mapView:(BMKMapView *)mapView onClickedMapBlank:(CLLocationCoordinate2D)coordinate {//地图单击代理
    if (singleTapCbid >= 0) {
        float lon = coordinate.longitude;
        float lat = coordinate.latitude;
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:3];
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:[NSNumber numberWithFloat:lon] forKey:@"lon"];
        [sendDict setObject:[NSNumber numberWithFloat:lat] forKey:@"lat"];
        [sendDict setObject:[NSNumber numberWithFloat:self.baiduMapView.zoomLevel] forKey:@"zoom"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.rotation] forKey:@"rotate"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.overlooking] forKey:@"overlook"];
        [self sendResultEventWithCallbackId:singleTapCbid dataDict:sendDict errDict:nil doDelete:NO];
    }
}

- (void)mapview:(BMKMapView *)mapView onDoubleClick:(CLLocationCoordinate2D)coordinate {//地图双击代理
    if (dubbleTapCbid >= 0) {
        float lon = coordinate.longitude;
        float lat = coordinate.latitude;
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:3];
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:[NSNumber numberWithFloat:lon] forKey:@"lon"];
        [sendDict setObject:[NSNumber numberWithFloat:lat] forKey:@"lat"];
        [sendDict setObject:[NSNumber numberWithFloat:self.baiduMapView.zoomLevel] forKey:@"zoom"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.rotation] forKey:@"rotate"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.overlooking] forKey:@"overlook"];
        [self sendResultEventWithCallbackId:dubbleTapCbid dataDict:sendDict errDict:nil doDelete:NO];
    }
}

- (void)mapview:(BMKMapView *)mapView onLongClick:(CLLocationCoordinate2D)coordinate {//地图长按代理
    if (longPressCbid >= 0) {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
        [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [sendDict setObject:[NSNumber numberWithFloat:coordinate.longitude] forKey:@"lon"];
        [sendDict setObject:[NSNumber numberWithFloat:coordinate.latitude] forKey:@"lat"];
        [sendDict setObject:[NSNumber numberWithFloat:self.baiduMapView.zoomLevel] forKey:@"zoom"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.rotation] forKey:@"rotate"];
        [sendDict setObject:[NSNumber numberWithInt:self.baiduMapView.overlooking] forKey:@"overlook"];
        [self sendResultEventWithCallbackId:longPressCbid dataDict:sendDict errDict:nil doDelete:NO];
    }
}

#pragma mark -
#pragma mark 路线搜索代理
#pragma mark -

- (void)onGetTransitRouteResult:(BMKRouteSearch *)searcher result:(BMKTransitRouteResult *)result errorCode:(BMKSearchErrorCode)error {//公交搜索结果代理
    if (error == BMK_SEARCH_NO_ERROR) {
        UZBMKRouteSearch *tempSearcher = (UZBMKRouteSearch *)searcher;
        NSString *routeIdStr = tempSearcher.searchRouteId;
        if (routeIdStr.length > 0) {
            NSArray *routes = result.routes;
            if (routes && routes.count>0) {
                [self.plans setObject:routes forKey:routeIdStr];
            }
        }
        NSMutableArray *allPlans = [NSMutableArray arrayWithCapacity:3];
        for (id plan in result.routes) {
            NSMutableDictionary *planOne = [NSMutableDictionary dictionaryWithCapacity:2];
            BMKTransitRouteLine *planLine = (BMKTransitRouteLine *)plan;
            NSInteger size = [planLine.steps count];
            //起点
            NSMutableDictionary *startInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            CLLocationCoordinate2D coords = planLine.starting.location;
            [startInfo setObject:[NSNumber numberWithFloat:coords.longitude] forKey:@"lon"];
            [startInfo setObject:[NSNumber numberWithFloat:coords.latitude] forKey:@"lat"];
            NSString *desStr = [NSString stringWithFormat:@"%@",planLine.starting.title];
            if (desStr.length>0) {
                [startInfo setObject:desStr forKey:@"description"];
            }
            if (startInfo.count > 0) {
                [planOne setObject:startInfo forKey:@"start"];
            }
            //终点
            NSMutableDictionary *endInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            CLLocationCoordinate2D coordsEnd = planLine.terminal.location;
            [endInfo setObject:[NSNumber numberWithFloat:coordsEnd.longitude] forKey:@"lon"];
            [endInfo setObject:[NSNumber numberWithFloat:coordsEnd.latitude] forKey:@"lat"];
            NSString *desStrEnd = [NSString stringWithFormat:@"%@",planLine.terminal.title];
            if (desStrEnd.length>0) {
                [endInfo setObject:desStrEnd forKey:@"description"];
            }
            if (endInfo.count > 0) {
                [planOne setObject:endInfo forKey:@"end"];
            }
            //结点
            NSMutableArray *allNodes= [NSMutableArray arrayWithCapacity:3];
            for (int i=0; i<size; i++) {
                if (i == size-1) {
                    continue;
                }
                BMKTransitStep *transitStep = [planLine.steps objectAtIndex:i];
                NSMutableDictionary *nodeStepInfo = [NSMutableDictionary dictionaryWithCapacity:2];
                CLLocationCoordinate2D coords = transitStep.entrace.location;
                [nodeStepInfo setObject:[NSNumber numberWithFloat:coords.longitude] forKey:@"lon"];
                [nodeStepInfo setObject:[NSNumber numberWithFloat:coords.latitude] forKey:@"lat"];
                NSString *desStr = transitStep.instruction;
                if ([desStr isKindOfClass:[NSString class]] && desStr.length>0) {
                    [nodeStepInfo setObject:desStr forKey:@"description"];
                }
                //int degree = transitStep.direction * 30;
                [nodeStepInfo setObject:[NSNumber numberWithInt:0] forKey:@"degree"];
                [allNodes addObject:nodeStepInfo];
            }
            if (allNodes.count > 0) {
                [planOne setObject:allNodes forKey:@"nodes"];
            }
            //距离&时间
            NSInteger distance = planLine.distance;
            float duration = planLine.duration.dates*(24*60*60) + planLine.duration.hours*(60*60) + planLine.duration.minutes*60 + planLine.duration.seconds;
            [planOne setObject:[NSNumber numberWithInteger:distance] forKey:@"distance"];
            [planOne setObject:[NSNumber numberWithFloat:duration] forKey:@"duration"];
            //添加到路线方案数组
            if (planOne.count > 0) {
                [allPlans addObject:planOne];
            }
        }
        //callback
        if (allPlans.count > 0) {
            NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
            [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
            [sendDict setObject:allPlans forKey:@"plans"];
            [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:nil doDelete:YES];
        } else {
            NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
            [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
            [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
        }
    } else {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
        [sendDict setObject:[NSNumber numberWithInteger:error] forKey:@"code"];
        NSArray *suggestStart = result.suggestAddrResult.startPoiList;
        NSArray *suggestEnd = result.suggestAddrResult.endPoiList;
        NSMutableArray *startAry = [NSMutableArray arrayWithCapacity:1];
        NSMutableArray *endAry = [NSMutableArray arrayWithCapacity:1];
        for (BMKPoiInfo *element in suggestStart) {
            NSString *name = element.name;
            if (![name isKindOfClass:[NSString class]] || name.length==0) {
                continue;
            }
            NSString *city = element.city;
            if (![city isKindOfClass:[NSString class]] || city.length==0) {
                continue;
            }
            CLLocationCoordinate2D coords = element.pt;
            double lon = coords.longitude;
            double lat = coords.latitude;
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            NSMutableDictionary *sugStar = [NSMutableDictionary dictionaryWithCapacity:3];
            [sugStar setObject:name forKey:@"name"];
            [sugStar setObject:city forKey:@"city"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lat"];
            [startAry addObject:sugStar];
        }
        for (BMKPoiInfo *element in suggestEnd) {
            NSString *name = element.name;
            if (![name isKindOfClass:[NSString class]] || name.length==0) {
                continue;
            }
            NSString *city = element.city;
            if (![city isKindOfClass:[NSString class]] || city.length==0) {
                continue;
            }
            CLLocationCoordinate2D coords = element.pt;
            double lon = coords.longitude;
            double lat = coords.latitude;
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            NSMutableDictionary *sugStar = [NSMutableDictionary dictionaryWithCapacity:3];
            [sugStar setObject:name forKey:@"name"];
            [sugStar setObject:city forKey:@"city"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lat"];
            [endAry addObject:sugStar];
        }
        if (startAry.count > 0) {
            [sendDict setObject:startAry forKey:@"suggestStarts"];
        }
        if (endAry.count > 0) {
            [sendDict setObject:endAry forKey:@"suggestEnds"];
        }
        [self sendResultEventWithCallbackId:searcRouteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:sendDict doDelete:YES];
    }
}

- (void)onGetDrivingRouteResult:(BMKRouteSearch *)searcher result:(BMKDrivingRouteResult *)result errorCode:(BMKSearchErrorCode)error {//驾乘搜索结果代理
    if (error == BMK_SEARCH_NO_ERROR) {
        UZBMKRouteSearch *tempSearcher = (UZBMKRouteSearch *)searcher;
        NSString *routeIdStr = tempSearcher.searchRouteId;
        if (routeIdStr.length > 0) {
            NSArray *routes = result.routes;
            if (routes && routes.count>0) {
                [self.plans setObject:routes forKey:routeIdStr];
            }
        }
        NSMutableArray *allPlans = [NSMutableArray arrayWithCapacity:3];
        for (id plan in result.routes) {
            NSMutableDictionary *planOne = [NSMutableDictionary dictionaryWithCapacity:2];
            BMKDrivingRouteLine *planLine = (BMKDrivingRouteLine *)plan;
            NSInteger size = [planLine.steps count];
            //起点
            NSMutableDictionary *startInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            CLLocationCoordinate2D coords = planLine.starting.location;
            [startInfo setObject:[NSNumber numberWithFloat:coords.longitude] forKey:@"lon"];
            [startInfo setObject:[NSNumber numberWithFloat:coords.latitude] forKey:@"lat"];
            NSString *desStr = [NSString stringWithFormat:@"%@",planLine.starting.title];
            if (desStr.length>0) {
                [startInfo setObject:desStr forKey:@"description"];
            }
            if (startInfo.count > 0) {
                [planOne setObject:startInfo forKey:@"start"];
            }
            //终点
            NSMutableDictionary *endInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            CLLocationCoordinate2D coordsEnd = planLine.terminal.location;
            [endInfo setObject:[NSNumber numberWithFloat:coordsEnd.longitude] forKey:@"lon"];
            [endInfo setObject:[NSNumber numberWithFloat:coordsEnd.latitude] forKey:@"lat"];
            NSString *desStrEnd = [NSString stringWithFormat:@"%@",planLine.terminal.title];
            if (desStrEnd.length>0) {
                [endInfo setObject:desStrEnd forKey:@"description"];
            }
            if (endInfo.count > 0) {
                [planOne setObject:endInfo forKey:@"end"];
            }
            //结点
            NSMutableArray *allNodes= [NSMutableArray arrayWithCapacity:3];
            for (int i=0; i<size; i++) {
                if (i == size-1) {
                    continue;
                }
                BMKDrivingStep *transitStep = [planLine.steps objectAtIndex:i];
                NSMutableDictionary *nodeStepInfo = [NSMutableDictionary dictionaryWithCapacity:2];
                CLLocationCoordinate2D coords = transitStep.entrace.location;
                [nodeStepInfo setObject:[NSNumber numberWithFloat:coords.longitude] forKey:@"lon"];
                [nodeStepInfo setObject:[NSNumber numberWithFloat:coords.latitude] forKey:@"lat"];
                NSString *desStr = transitStep.instruction;
                if ([desStr isKindOfClass:[NSString class]] && desStr.length>0) {
                    [nodeStepInfo setObject:desStr forKey:@"description"];
                }
                int degree = transitStep.direction * 30;
                [nodeStepInfo setObject:[NSNumber numberWithInt:degree] forKey:@"degree"];
                //路线点集合
                NSMutableArray *pointsAry = [NSMutableArray arrayWithCapacity:1];
                for (int j=0; j<transitStep.pointsCount; j++) {
                    NSMutableDictionary *pointsDict = [NSMutableDictionary dictionaryWithCapacity:1];
                    [pointsDict setObject:[NSNumber numberWithDouble:transitStep.points[j].x] forKey:@"x"];
                    [pointsDict setObject:[NSNumber numberWithDouble:transitStep.points[j].y] forKey:@"y"];
                    [pointsAry addObject:pointsDict];
                }
                [allNodes addObject:nodeStepInfo];
            }
            if (allNodes.count > 0) {
                [planOne setObject:allNodes forKey:@"nodes"];
            }
            //距离&时间
            NSInteger distance = planLine.distance;
            float duration = planLine.duration.dates*(24*60*60) + planLine.duration.hours*(60*60) + planLine.duration.minutes*60 + planLine.duration.seconds;
            [planOne setObject:[NSNumber numberWithInteger:distance] forKey:@"distance"];
            [planOne setObject:[NSNumber numberWithFloat:duration] forKey:@"duration"];
            //添加到路线方案数组
            if (planOne.count > 0) {
                [allPlans addObject:planOne];
            }
        }
        //callback
        if (allPlans.count > 0) {
            NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
            [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
            [sendDict setObject:allPlans forKey:@"plans"];
            [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:nil doDelete:YES];
        } else {
            NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
            [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
            [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
        }
    } else {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
        [sendDict setObject:[NSNumber numberWithInteger:error] forKey:@"code"];
        NSArray *suggestStart = result.suggestAddrResult.startPoiList;
        NSArray *suggestEnd = result.suggestAddrResult.endPoiList;
        NSMutableArray *startAry = [NSMutableArray arrayWithCapacity:1];
        NSMutableArray *endAry = [NSMutableArray arrayWithCapacity:1];
        for (BMKPoiInfo *element in suggestStart) {
            NSString *name = element.name;
            if (![name isKindOfClass:[NSString class]] || name.length==0) {
                continue;
            }
            NSString *city = element.city;
            if (![city isKindOfClass:[NSString class]] || city.length==0) {
                continue;
            }
            CLLocationCoordinate2D coords = element.pt;
            double lon = coords.longitude;
            double lat = coords.latitude;
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            NSMutableDictionary *sugStar = [NSMutableDictionary dictionaryWithCapacity:3];
            [sugStar setObject:name forKey:@"name"];
            [sugStar setObject:city forKey:@"city"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lat"];
            [startAry addObject:sugStar];
        }
        for (BMKPoiInfo *element in suggestEnd) {
            NSString *name = element.name;
            if (![name isKindOfClass:[NSString class]] || name.length==0) {
                continue;
            }
            NSString *city = element.city;
            if (![city isKindOfClass:[NSString class]] || city.length==0) {
                continue;
            }
            CLLocationCoordinate2D coords = element.pt;
            double lon = coords.longitude;
            double lat = coords.latitude;
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            NSMutableDictionary *sugStar = [NSMutableDictionary dictionaryWithCapacity:3];
            [sugStar setObject:name forKey:@"name"];
            [sugStar setObject:city forKey:@"city"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lat"];
            [endAry addObject:sugStar];
        }
        if (startAry.count > 0) {
            [sendDict setObject:startAry forKey:@"suggestStarts"];
        }
        if (endAry.count > 0) {
            [sendDict setObject:endAry forKey:@"suggestEnds"];
        }
        [self sendResultEventWithCallbackId:searcRouteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:sendDict doDelete:YES];
    }
}

- (void)onGetWalkingRouteResult:(BMKRouteSearch *)searcher result:(BMKWalkingRouteResult *)result errorCode:(BMKSearchErrorCode)error {//步行搜索结代理
    if (error == BMK_SEARCH_NO_ERROR) {
        UZBMKRouteSearch *tempSearcher = (UZBMKRouteSearch *)searcher;
        NSString *routeIdStr = tempSearcher.searchRouteId;
        if (routeIdStr.length > 0) {
            NSArray *routes = result.routes;
            if (routes && routes.count>0) {
                [self.plans setObject:routes forKey:routeIdStr];
            }
        }
        NSMutableArray *allPlans = [NSMutableArray arrayWithCapacity:3];
        for (id plan in result.routes) {
            NSMutableDictionary *planOne = [NSMutableDictionary dictionaryWithCapacity:2];
            BMKWalkingRouteLine *planLine = (BMKWalkingRouteLine *)plan;
            NSInteger size = [planLine.steps count];
            //起点
            NSMutableDictionary *startInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            CLLocationCoordinate2D coords = planLine.starting.location;
            [startInfo setObject:[NSNumber numberWithFloat:coords.longitude] forKey:@"lon"];
            [startInfo setObject:[NSNumber numberWithFloat:coords.latitude] forKey:@"lat"];
            NSString *desStr = [NSString stringWithFormat:@"%@",planLine.starting.title];
            if (desStr.length>0) {
                [startInfo setObject:desStr forKey:@"description"];
            }
            if (startInfo.count > 0) {
                [planOne setObject:startInfo forKey:@"start"];
            }
            //终点
            NSMutableDictionary *endInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            CLLocationCoordinate2D coordsEnd = planLine.terminal.location;
            [endInfo setObject:[NSNumber numberWithFloat:coordsEnd.longitude] forKey:@"lon"];
            [endInfo setObject:[NSNumber numberWithFloat:coordsEnd.latitude] forKey:@"lat"];
            NSString *desStrEnd = [NSString stringWithFormat:@"%@",planLine.terminal.title];
            if ([desStrEnd isKindOfClass:[NSString class]] && desStrEnd.length>0) {
                [endInfo setObject:desStrEnd forKey:@"description"];
            }
            if (endInfo.count > 0) {
                [planOne setObject:endInfo forKey:@"end"];
            }
            //结点
            NSMutableArray *allNodes= [NSMutableArray arrayWithCapacity:3];
            for (int i=0; i<size; i++) {
                if (i == size-1) {
                    continue;
                }
                BMKWalkingStep *transitStep = [planLine.steps objectAtIndex:i];
                NSMutableDictionary *nodeStepInfo = [NSMutableDictionary dictionaryWithCapacity:2];
                CLLocationCoordinate2D coords = transitStep.entrace.location;
                [nodeStepInfo setObject:[NSNumber numberWithFloat:coords.longitude] forKey:@"lon"];
                [nodeStepInfo setObject:[NSNumber numberWithFloat:coords.latitude] forKey:@"lat"];
                NSString *desStr = transitStep.instruction;
                if ([desStr isKindOfClass:[NSString class]] && desStr.length>0) {
                    [nodeStepInfo setObject:desStr forKey:@"description"];
                }
                int degree = transitStep.direction * 30;
                [nodeStepInfo setObject:[NSNumber numberWithInt:degree] forKey:@"degree"];
                [allNodes addObject:nodeStepInfo];
            }
            if (allNodes.count > 0) {
                [planOne setObject:allNodes forKey:@"nodes"];
            }
            //距离&时间
            NSInteger distance = planLine.distance;
            float duration = planLine.duration.dates*(24*60*60) + planLine.duration.hours*(60*60) + planLine.duration.minutes*60 + planLine.duration.seconds;
            [planOne setObject:[NSNumber numberWithInteger:distance] forKey:@"distance"];
            [planOne setObject:[NSNumber numberWithFloat:duration] forKey:@"duration"];
            //添加到路线方案数组
            if (planOne.count > 0) {
                [allPlans addObject:planOne];
            }
        }
        //callback
        if (allPlans.count > 0) {
            NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
            [sendDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
            [sendDict setObject:allPlans forKey:@"plans"];
            [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:nil doDelete:YES];
        } else {
            NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
            [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
            [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
        }
    } else {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
        [sendDict setObject:[NSNumber numberWithInteger:error] forKey:@"code"];
        NSArray *suggestStart = result.suggestAddrResult.startPoiList;
        NSArray *suggestEnd = result.suggestAddrResult.endPoiList;
        NSMutableArray *startAry = [NSMutableArray arrayWithCapacity:1];
        NSMutableArray *endAry = [NSMutableArray arrayWithCapacity:1];
        for (BMKPoiInfo *element in suggestStart) {
            NSString *name = element.name;
            if (![name isKindOfClass:[NSString class]] || name.length==0) {
                continue;
            }
            NSString *city = element.city;
            if (![city isKindOfClass:[NSString class]] || city.length==0) {
                continue;
            }
            CLLocationCoordinate2D coords = element.pt;
            double lon = coords.longitude;
            double lat = coords.latitude;
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            NSMutableDictionary *sugStar = [NSMutableDictionary dictionaryWithCapacity:3];
            [sugStar setObject:name forKey:@"name"];
            [sugStar setObject:city forKey:@"city"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lat"];
            [startAry addObject:sugStar];
        }
        for (BMKPoiInfo *element in suggestEnd) {
            NSString *name = element.name;
            if (![name isKindOfClass:[NSString class]] || name.length==0) {
                continue;
            }
            NSString *city = element.city;
            if (![city isKindOfClass:[NSString class]] || city.length==0) {
                continue;
            }
            CLLocationCoordinate2D coords = element.pt;
            double lon = coords.longitude;
            double lat = coords.latitude;
            if (![self isValidLon:lon lat:lat]) {
                continue;
            }
            NSMutableDictionary *sugStar = [NSMutableDictionary dictionaryWithCapacity:3];
            [sugStar setObject:name forKey:@"name"];
            [sugStar setObject:city forKey:@"city"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
            [sugStar setObject:[NSNumber numberWithDouble:lon] forKey:@"lat"];
            [endAry addObject:sugStar];
        }
        if (startAry.count > 0) {
            [sendDict setObject:startAry forKey:@"suggestStarts"];
        }
        if (endAry.count > 0) {
            [sendDict setObject:endAry forKey:@"suggestEnds"];
        }
        [self sendResultEventWithCallbackId:searcRouteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:sendDict doDelete:YES];
    }
}

#pragma mark -
#pragma mark 百度地图管理区代理
#pragma mark -

- (void)onGetNetworkState:(int)iError {
    if (iError != 0) {
        BMKMapManager *manager = [UZAppUtils globalValueForKey:@"BMKMapManager"];
        if (manager) {
            NSDictionary *feature_location = [self getFeatureByName:@"bMap"];
            NSString *ios_api_key = [feature_location stringValueForKey:@"ios_api_key" defaultValue:nil];
            if (!ios_api_key || ios_api_key.length==0) {
                NSDictionary *feature_map = [self getFeatureByName:@"baiduLocation"];
                ios_api_key = [feature_map stringValueForKey:@"ios_api_key" defaultValue:nil];
            }
            if (!ios_api_key || ios_api_key.length==0) {
                NSDictionary *feature_map = [self getFeatureByName:@"baiduMap"];
                ios_api_key = [feature_map stringValueForKey:@"ios_api_key" defaultValue:nil];
            }
            if (!ios_api_key) {
                ios_api_key = [feature_location stringValueForKey:@"apiKey" defaultValue:nil];
            }
            if (!ios_api_key) {
                ios_api_key = @"key";
            }
            [manager start:ios_api_key generalDelegate:self];
        } else {
            [self initMapSDK];
        }
    }
}

- (void)onGetPermissionState:(int)iError {
    if (iError != 0) {
        BMKMapManager *manager = [UZAppUtils globalValueForKey:@"BMKMapManager"];
        if (manager) {
            NSDictionary *feature_location = [self getFeatureByName:@"bMap"];
            NSString *ios_api_key = [feature_location stringValueForKey:@"ios_api_key" defaultValue:nil];
            if (!ios_api_key || ios_api_key.length==0) {
                NSDictionary *feature_map = [self getFeatureByName:@"baiduLocation"];
                ios_api_key = [feature_map stringValueForKey:@"ios_api_key" defaultValue:nil];
            }
            if (!ios_api_key || ios_api_key.length==0) {
                NSDictionary *feature_map = [self getFeatureByName:@"baiduMap"];
                ios_api_key = [feature_map stringValueForKey:@"ios_api_key" defaultValue:nil];
            }
            if (!ios_api_key) {
                ios_api_key = [feature_location stringValueForKey:@"apiKey" defaultValue:nil];
            }
            if (!ios_api_key) {
                ios_api_key = @"key";
            }
            [manager start:ios_api_key generalDelegate:self];
        } else {
            [self initMapSDK];
        }
    }
}

#pragma mark -
#pragma mark 地址、经纬度转换代理
#pragma mark -

- (void)onGetGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error {//地址信息搜索结果代理
    if (error == BMK_SEARCH_NO_ERROR) {
        NSMutableDictionary *cbDict = [NSMutableDictionary dictionaryWithCapacity:2];
        [cbDict setObject:[NSNumber numberWithFloat:result.location.longitude] forKey:@"lon"];
        [cbDict setObject:[NSNumber numberWithFloat:result.location.latitude] forKey:@"lat"];
        [cbDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [self sendResultEventWithCallbackId:getLocFromAddrCbid dataDict:cbDict errDict:nil  doDelete:YES];
    } else {
        int errCode = 0;
        switch (error) {
            case BMK_SEARCH_AMBIGUOUS_KEYWORD:
                errCode = 1;
                break;
                
            case BMK_SEARCH_AMBIGUOUS_ROURE_ADDR:
                errCode = 2;
                break;
                
            case BMK_SEARCH_RESULT_NOT_FOUND:
                errCode = 3;
                break;
                
            case BMK_SEARCH_KEY_ERROR:
                errCode = 4;
                break;
                
            case BMK_SEARCH_NETWOKR_ERROR:
                errCode = 5;
                break;
                
            case BMK_SEARCH_NETWOKR_TIMEOUT:
                errCode = 6;
                break;
                
            case BMK_SEARCH_PERMISSION_UNFINISHED: {
                UZBMKGeoCodeSearch *temp = (UZBMKGeoCodeSearch *)searcher;
                BMKGeoCodeSearchOption *addrInfo = [[BMKGeoCodeSearchOption alloc]init];
                addrInfo.address = temp.address;
                addrInfo.city = temp.city;
                BOOL isSearch = [self.geoSearch geoCode:addrInfo];
                //if (!isSearch) {
                    //[self sendResultEventWithCallbackId:getLocFromAddrCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
                //}
                return;
            }
                break;
                
            default:
                break;
        }
        NSMutableDictionary *cbDict = [NSMutableDictionary dictionaryWithCapacity:2];
        [cbDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
        [self sendResultEventWithCallbackId:getLocFromAddrCbid dataDict:cbDict errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:errCode] forKey:@"code"]  doDelete:YES];
    }
}

- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error {//地理编码搜索结果代理（根据经纬度返回地址）
    if (error == BMK_SEARCH_NO_ERROR) {
        UZBMKGeoCodeSearch *tempGeo = (UZBMKGeoCodeSearch *)searcher;
        if (tempGeo.type == SEARCH_ROUTE) {
            BMKPlanNode *start = [tempGeo.transitInfo objectForKey:@"start"];
            BMKPlanNode *end = [tempGeo.transitInfo objectForKey:@"end"];
            NSString *routeId = [tempGeo.transitInfo objectForKey:@"routeId"];
            int transPlicy = [[tempGeo.transitInfo objectForKey:@"transPlicy"]intValue];
            BMKTransitPolicy policy;
            switch (transPlicy) {
                case 3:
                    policy = BMK_TRANSIT_TIME_FIRST;
                    break;
                    
                case 4:
                    policy = BMK_TRANSIT_TRANSFER_FIRST;
                    break;
                    
                case 5:
                    policy = BMK_TRANSIT_WALK_FIRST;
                    break;
                    
                case 6:
                    policy = BMK_TRANSIT_NO_SUBWAY;
                    break;
                    
                default:
                    policy = BMK_TRANSIT_TIME_FIRST;
                    break;
            }
            UZBMKRouteSearch *routeSearcher = [[UZBMKRouteSearch alloc] init];
            routeSearcher.delegate = self;
            routeSearcher.searchRouteId = routeId;
            BMKTransitRoutePlanOption *transInfo = [[BMKTransitRoutePlanOption alloc]init];
            transInfo.transitPolicy = policy;
            transInfo.from = start;
            transInfo.to = end;
            NSString *city = result.addressDetail.city;
            if ([city isKindOfClass:[NSString class]] && city.length>0) {
                transInfo.city = city;
            } else {
                transInfo.city = @"北京";
            }
            BOOL isSecceed = [routeSearcher transitSearch:transInfo];
            if (!isSecceed) {
                NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:2];
                [sendDict setObject:[NSNumber numberWithBool:NO] forKey:@"status"];
                [self sendResultEventWithCallbackId:searcRouteCbid dataDict:sendDict errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:-1] forKey:@"code"] doDelete:YES];
            }
            return;
        }
        BMKAddressComponent *addCompent = result.addressDetail;
        NSString *addr = result.address;
        NSString *province = addCompent.province;
        NSString *city = addCompent.city;
        NSString *district = addCompent.district;
        NSString *streetName = addCompent.streetName;
        NSString *streetNumber = addCompent.streetNumber;
        NSMutableDictionary *cbDict = [NSMutableDictionary dictionaryWithCapacity:2];
        if ([addr isKindOfClass:[NSString class]] && addr.length>0) {
            [cbDict setObject:addr forKey:@"address"];
        }
        [cbDict setObject:[NSNumber numberWithFloat:result.location.longitude] forKey:@"lon"];
        [cbDict setObject:[NSNumber numberWithFloat:result.location.latitude]  forKey:@"lat"];
        if ([province isKindOfClass:[NSString class]] && province.length>0) {
            [cbDict setObject:province forKey:@"province"];
        }
        if ([city isKindOfClass:[NSString class]] && city.length>0) {
            [cbDict setObject:city forKey:@"city"];
        }
        if ([district isKindOfClass:[NSString class]] && district.length>0) {
            [cbDict setObject:district forKey:@"district"];
        }
        if ([streetName isKindOfClass:[NSString class]] && streetName.length>0) {
            [cbDict setObject:streetName forKey:@"streetName"];
        }
        if ([streetNumber isKindOfClass:[NSString class]] && streetNumber.length>0) {
            [cbDict setObject:streetNumber forKey:@"streetNumber"];
        }
        [cbDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];

        NSArray *poiAry = result.poiList;
        NSMutableArray *poiInfoAry = [NSMutableArray array];
        for (BMKPoiInfo *poiInfo in poiAry) {
            NSMutableDictionary *pointInfo = [NSMutableDictionary dictionary];
            NSString *name = poiInfo.name;
            if (![name isKindOfClass:[NSString class]] || name.length==0) {
                name = @"";
            }
            [pointInfo setObject:name forKey:@"name"];
            NSString *uid = poiInfo.uid;
            if (![uid isKindOfClass:[NSString class]] || uid.length==0) {
                uid = @"";
            }
            [pointInfo setObject:uid forKey:@"uid"];
            NSString *address = poiInfo.address;
            if (![address isKindOfClass:[NSString class]] || address.length==0) {
                address = @"";
            }
            [pointInfo setObject:address forKey:@"address"];
            NSString *city = poiInfo.city;
            if (![city isKindOfClass:[NSString class]] || city.length==0) {
                city = @"";
            }
            [pointInfo setObject:city forKey:@"city"];
            NSString *phone = poiInfo.phone;
            if (![phone isKindOfClass:[NSString class]] || phone.length==0) {
                phone = @"";
            }
            [pointInfo setObject:phone forKey:@"phone"];
            NSString *postcode = poiInfo.postcode;
            if (![postcode isKindOfClass:[NSString class]] || postcode.length==0) {
                postcode = @"";
            }
            [pointInfo setObject:postcode forKey:@"postcode"];
            int epoitype = poiInfo.epoitype;
            [pointInfo setObject:[NSNumber numberWithInt:epoitype] forKey:@"epoitype"];
            NSMutableDictionary *pointPt = [NSMutableDictionary dictionary];
            float lat = poiInfo.pt.latitude;
            float lon = poiInfo.pt.longitude;
            [pointPt setObject:[NSNumber numberWithFloat:lat] forKey:@"lat"];
            [pointPt setObject:[NSNumber numberWithFloat:lon] forKey:@"lon"];
            [pointInfo setObject:pointPt forKey:@"coord"];
            [poiInfoAry addObject:pointInfo];
        }
        [cbDict setObject:poiInfoAry forKey:@"poiList"];
        [self sendResultEventWithCallbackId:getAddrFromLoc dataDict:cbDict errDict:nil  doDelete:YES];
    } else {
        int errCode = 0;
        switch (error) {
            case BMK_SEARCH_AMBIGUOUS_KEYWORD:
                errCode = 1;
                break;
                
            case BMK_SEARCH_AMBIGUOUS_ROURE_ADDR:
                errCode = 2;
                break;
                
            case BMK_SEARCH_RESULT_NOT_FOUND:
                errCode = 3;
                break;
                
            case BMK_SEARCH_KEY_ERROR:
                errCode = 4;
                break;
                
            case BMK_SEARCH_NETWOKR_ERROR:
                errCode = 5;
                break;
                
            case BMK_SEARCH_NETWOKR_TIMEOUT:
                errCode = 6;
                break;
                
            case BMK_SEARCH_PERMISSION_UNFINISHED:
                errCode = 7;
                break;
                
            default:
                break;
        }
        [self sendResultEventWithCallbackId:getAddrFromLoc dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:errCode] forKey:@"code"]  doDelete:YES];
    }
}

#pragma mark-
#pragma mark 定位代理函数
#pragma mark-

- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation {//用户方向更新后代理
    [_baiduMapView updateLocationData:userLocation];
}

- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation {//用户位置更新后代理
    [_baiduMapView updateLocationData:userLocation];
    [self showCurrentLocation:userLocation];
    [self getCurrentLocation:userLocation];
}

- (void)didFailToLocateUserWithError:(NSError *)error {//定位失败代理
    if (locationStarted) {
        locationStarted = NO;
        [_locService stopUserLocationService];
        NSString *msg = [error localizedDescription];
        if ([msg isKindOfClass:[NSString class]]) {
            NSDictionary *ret = @{@"status":@(NO)};
            NSDictionary *err = @{@"code":[NSNumber numberWithInteger:error.code],@"msg":msg};
            [self sendResultEventWithCallbackId:startLocationCbid dataDict:ret errDict:err doDelete:YES];
        }
    }
}

#pragma mark -
#pragma mark 手势代理（屏蔽右侧滑退出controller）
#pragma mark -

- (void)handleSingleTap:(UIGestureRecognizer *)gesture {
}

#pragma mark -
#pragma mark 工具函数
#pragma mark -

- (void)initOfflineMap {
    if (!_offlineMap) {
        _offlineMap = [[BMKOfflineMap alloc]init];
        _offlineMap.delegate = self;
    }
}

- (NSString *)getMyBundlePath:(NSString *)filename {//获取百度bundle内图片路径
    NSBundle *libBundle = MYBUNDLE;
    if (libBundle && filename){
        NSString *s=[[libBundle resourcePath]stringByAppendingPathComponent:filename];
        return s;
    }
    return nil ;
}

- (BOOL)isValidLon:(float)lon lat:(float)lat {//判断经纬度是否合法
    if (ABS(lon)>180 || ABS(lat)>90) {
        return NO;
    }
    return YES;
}

- (void)selectBubbleIllus:(UIButton *)btn {//点击气泡上的插图事件
    if (setBubbleCbid >= 0) {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
        [sendDict setObject:[NSNumber numberWithInteger:btn.tag] forKey:@"id"];
        [sendDict setObject:@"clickIllus" forKey:@"eventType"];
        [self sendResultEventWithCallbackId:setBubbleCbid dataDict:sendDict errDict:nil doDelete:NO];
    }
}

- (void)selectBillboard:(UIButton *)btn {//点击布告牌事件
    if (addBillboardCbid >= 0) {
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
        [sendDict setObject:[NSNumber numberWithInteger:btn.tag] forKey:@"id"];
        [self sendResultEventWithCallbackId:addBillboardCbid dataDict:sendDict errDict:nil doDelete:NO];
    }
}

- (void)addBillboard:(UZbMapAnnotation *)tempAnnot with:(BMKPinAnnotationView *)pinAnnotationView {//添加自定义的布告牌
    //背景图片（160*75）
    NSString *pinIconPath = tempAnnot.pinImg;
    if (pinIconPath && [pinIconPath isKindOfClass:[NSString class]] && pinIconPath.length>0) {
        NSString *pinRealImg = [self getPathWithUZSchemeURL:pinIconPath];
        UIImage *bgImg = [UIImage imageWithContentsOfFile:pinRealImg];
        UIImageView *bgView = [[UIImageView alloc]init];
        bgView.frame = pinAnnotationView.bounds;
        bgView.image = bgImg;
        [pinAnnotationView addSubview:bgView];
    }
    //内容
    NSDictionary *contentInfo = [NSDictionary dictionaryWithDictionary:tempAnnot.content];
    NSDictionary *stylesInfo = tempAnnot.styles;
    NSString *aligment = [stylesInfo stringValueForKey:@"illusAlign" defaultValue:@"left"];
    NSString *illusPath = [contentInfo stringValueForKey:@"illus" defaultValue:nil];
    NSString *titleColor = [stylesInfo stringValueForKey:@"titleColor" defaultValue:@"#000"];
    NSString *subTitleColor = [stylesInfo stringValueForKey:@"subTitleColor" defaultValue:@"#000"];
    float titleSize = [stylesInfo floatValueForKey:@"titleSize" defaultValue:16];
    float subtitleSize = [stylesInfo floatValueForKey:@"subTitleSize" defaultValue:12];
    //位置参数
    float labelW, labelH, labelX ,labelY;
    labelY = 10;
    labelH = 20;
    labelW = 100;
    //插图
    BOOL hasIllus = YES;
    if (illusPath.length > 0) {
        CGRect rect;
        if ([illusPath hasPrefix:@"http"]) {
            BMapAsyncImageView *asyImg = [[BMapAsyncImageView alloc]init];
            [asyImg loadImage:illusPath];
            if (aligment.length>0 && [aligment isEqualToString:@"right"]) {
                rect = CGRectMake(115, 5, 35, 50);
                labelX = 10;
            } else {
                rect = CGRectMake(10, 5, 35, 50);
                labelX = 50;
            }
            asyImg.frame = rect;
            [pinAnnotationView addSubview:asyImg];
        } else {
            NSString *realIllusPath = [self getPathWithUZSchemeURL:illusPath];
            UIImageView *illusImg = [[UIImageView alloc]init];
            illusImg.image = [UIImage imageWithContentsOfFile:realIllusPath];
            if (aligment.length>0 && [aligment isEqualToString:@"right"]) {
                rect = CGRectMake(115, 5, 35, 50);
                labelX = 10;
            } else {
                rect = CGRectMake(10, 5, 35, 50);
                labelX = 50;
            }
            illusImg.frame = rect;
            [pinAnnotationView addSubview:illusImg];
        }
    } else {
        labelX = 10;
        labelW = 140;
        hasIllus = NO;
    }
    //标题
    NSString *title = [contentInfo stringValueForKey:@"title" defaultValue:@""];
    UILabel *titleLab = [[UILabel alloc]initWithFrame:CGRectMake(labelX, labelY, labelW, labelH)];
    titleLab.text = title;
    titleLab.backgroundColor = [UIColor clearColor];
    titleLab.font = [UIFont systemFontOfSize:titleSize];
    titleLab.textColor = [UZAppUtils colorFromNSString:titleColor];
    if (!hasIllus) {
        titleLab.textAlignment = NSTextAlignmentCenter;
    }
    [pinAnnotationView addSubview:titleLab];
    //子标题
    NSString *subTitle = [contentInfo stringValueForKey:@"subTitle" defaultValue:nil];
    UILabel *subTitleLab = [[UILabel alloc]initWithFrame:CGRectMake(labelX, labelY+labelH, labelW, labelH)];
    subTitleLab.text = subTitle;
    subTitleLab.backgroundColor = [UIColor clearColor];
    subTitleLab.font = [UIFont systemFontOfSize:subtitleSize];
    subTitleLab.textColor = [UZAppUtils colorFromNSString:subTitleColor];
    if (!hasIllus) {
        titleLab.textAlignment = NSTextAlignmentCenter;
    }
    [pinAnnotationView addSubview:subTitleLab];
}

- (void)addAnnotaion:(UZbMapAnnotation *)tempAnnot with:(BMKPinAnnotationView *)pinAnnotationView {//添加自定义的标注
    //自定义大头针
    NSString *pinIconPath = tempAnnot.pinImg;//大头针图标
    if (pinIconPath && [pinIconPath isKindOfClass:[NSString class]] && pinIconPath.length>0) {
        NSString *pinRealImg = [self getPathWithUZSchemeURL:pinIconPath];
        UIImage *pinImage = [UIImage imageWithContentsOfFile:pinRealImg];
        if (pinImage) {
            pinAnnotationView.image = pinImage;
            pinAnnotationView.centerOffset = CGPointMake(0, -pinAnnotationView.bounds.size.height/2);
        }
    }
    //气泡
    NSDictionary *contentInfo = [NSDictionary dictionaryWithDictionary:tempAnnot.content];
    NSDictionary *stylesInfo = tempAnnot.styles;
    NSString *aligment = [stylesInfo stringValueForKey:@"illusAlign" defaultValue:@"left"];
    NSString *illusPath = [contentInfo stringValueForKey:@"illus" defaultValue:nil];
    NSString *titleColor = [stylesInfo stringValueForKey:@"titleColor" defaultValue:@"#000"];
    NSString *subTitleColor = [stylesInfo stringValueForKey:@"subTitleColor" defaultValue:@"#000"];
    float titleSize = [stylesInfo floatValueForKey:@"titleSize" defaultValue:16];
    float subtitleSize = [stylesInfo floatValueForKey:@"subTitleSize" defaultValue:12];
    NSString *title = [contentInfo stringValueForKey:@"title" defaultValue:@""];
    CGSize feelSize = [title sizeWithFont:[UIFont systemFontOfSize:titleSize] constrainedToSize:CGSizeMake(400,100)];
    float bubbleLong = feelSize.width;
    if (bubbleLong > _baiduMapView.bounds.size.width-64) {
        bubbleLong = _baiduMapView.bounds.size.width - 64;
    }
    if (bubbleLong < 160) {
        bubbleLong = 160;
    }
    //背景图片
    UIImageView *popView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, bubbleLong, 90)];
    NSString *realPath = [self getPathWithUZSchemeURL:tempAnnot.bubbleBgImg];
    UIImage *imageBg = nil;
    if (realPath.length > 0) {
        imageBg = [UIImage imageWithContentsOfFile:realPath];
    } else {
        realPath = [[NSBundle mainBundle]pathForResource:@"res_bMap/bubble" ofType:@"png"];
        imageBg = [UIImage imageWithContentsOfFile:realPath];
        //右边气泡
        NSString *leftBubble = [[NSBundle mainBundle]pathForResource:@"res_bMap/bubble_left" ofType:@"png"];
        UIImage *leftImage = [UIImage imageWithContentsOfFile:leftBubble];
        UIEdgeInsets inset1 = UIEdgeInsetsMake(10, 10, 16, 16);
        leftImage = [leftImage resizableImageWithCapInsets:inset1 resizingMode:UIImageResizingModeStretch];
        UIImageView *leftImg = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, bubbleLong/2.0, 90)];
        leftImg.image = leftImage;
        [popView addSubview:leftImg];
        leftImg.userInteractionEnabled = NO;
        //右边气泡
        NSString *rightBubble = [[NSBundle mainBundle]pathForResource:@"res_bMap/bubble_right" ofType:@"png"];
        UIImage *rightImage = [UIImage imageWithContentsOfFile:rightBubble];
        UIEdgeInsets inset2 = UIEdgeInsetsMake(10, 16, 16, 10);
        rightImage = [rightImage resizableImageWithCapInsets:inset2 resizingMode:UIImageResizingModeStretch];
        UIImageView *rightImg = [[UIImageView alloc]initWithFrame:CGRectMake(bubbleLong/2.0, 0, bubbleLong/2.0, 90)];
        rightImg.image = rightImage;
        [popView addSubview:rightImg];
        rightImg.userInteractionEnabled = NO;
    }
    popView.image = imageBg;
    popView.userInteractionEnabled = YES;
    //位置参数
    float labelW, labelH, labelX ,labelY;
    labelY = 18;
    labelH = 20;
    labelW = bubbleLong - 55;
    //插图
    if (illusPath.length > 0) {
        CGRect rect;
        if ([illusPath hasPrefix:@"http"]) {
            BMapAsyncImageView *asyImg = [[BMapAsyncImageView alloc]init];
            [asyImg loadImage:illusPath];
            if (aligment.length>0 && [aligment isEqualToString:@"right"]) {
                rect = CGRectMake(bubbleLong-40, labelY, 30, 40);
                labelX = 10;
            } else {
                rect = CGRectMake(10, labelY, 30, 40);
                labelX = 45;
            }
            asyImg.frame = rect;
            asyImg.userInteractionEnabled = YES;
            [popView addSubview:asyImg];
            //添加单击事件
            UIButton *tap = [UIButton buttonWithType:UIButtonTypeCustom];
            tap.frame = asyImg.bounds;
            tap.tag= tempAnnot.annoId;
            [tap addTarget:self action:@selector(selectBubbleIllus:) forControlEvents:UIControlEventTouchDown];
            [asyImg addSubview:tap];
        } else {
            NSString *realIllusPath = [self getPathWithUZSchemeURL:illusPath];
            UIImageView *illusImg = [[UIImageView alloc]init];
            illusImg.image = [UIImage imageWithContentsOfFile:realIllusPath];
            if (aligment.length>0 && [aligment isEqualToString:@"right"]) {
                rect = CGRectMake(bubbleLong-40, labelY, 30, 40);
                labelX = 10;
            } else {
                rect = CGRectMake(10, labelY, 30, 40);
                labelX = 45;
            }
            illusImg.frame = rect;
            illusImg.userInteractionEnabled = YES;
            [popView addSubview:illusImg];
            //添加单击事件
            UIButton *tap = [UIButton buttonWithType:UIButtonTypeCustom];
            tap.frame = illusImg.bounds;
            tap.tag= tempAnnot.annoId;
            [tap addTarget:self action:@selector(selectBubbleIllus:) forControlEvents:UIControlEventTouchDown];
            [illusImg addSubview:tap];
        }
    } else {
        labelX = 10;
        labelW = bubbleLong - 20;
    }
    //标题
    UILabel *titleLab = [[UILabel alloc]initWithFrame:CGRectMake(labelX, labelY, labelW, labelH)];
    titleLab.text = title;
    titleLab.backgroundColor = [UIColor clearColor];
    titleLab.font = [UIFont systemFontOfSize:titleSize];
    titleLab.textColor = [UZAppUtils colorFromNSString:titleColor];
    [popView addSubview:titleLab];
    //子标题
    NSString *subTitle = [contentInfo stringValueForKey:@"subTitle" defaultValue:nil];
    UILabel *subTitleLab = [[UILabel alloc]initWithFrame:CGRectMake(labelX, labelY+labelH+5, labelW, labelH)];
    subTitleLab.text = subTitle;
    subTitleLab.backgroundColor = [UIColor clearColor];
    subTitleLab.font = [UIFont systemFontOfSize:subtitleSize];
    subTitleLab.textColor = [UZAppUtils colorFromNSString:subTitleColor];
    [popView addSubview:subTitleLab];
    //添加气泡
    BMKActionPaopaoView *pView = [[BMKActionPaopaoView alloc]initWithCustomView:popView];
    pView.frame = CGRectMake(0, 0, bubbleLong, 90);
    pinAnnotationView.paopaoView = pView;
}

- (BMKAnnotationView *)getDefaultAnnotaionWith:(UZbMapAnnotation *)tempAnnot andMap:(BMKMapView *)mapView {//添加布告牌、标注
    NSString *identifie = [NSString stringWithFormat:@"%ld",(long)tempAnnot.annoId];
    BMKPinAnnotationView *pinAnnotationView = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:identifie];
    if (pinAnnotationView == nil){
        pinAnnotationView = [[BMKPinAnnotationView alloc]initWithAnnotation:tempAnnot reuseIdentifier:identifie];
    }
    pinAnnotationView.draggable = tempAnnot.draggable;
    pinAnnotationView.animatesDrop = NO;
    if (tempAnnot.isStyled) {
        pinAnnotationView.canShowCallout = YES;
    } else {
        pinAnnotationView.canShowCallout = NO;
    }
    switch (tempAnnot.type) {
        case ANNOTATION_MARKE://添加自定义的标注
            [self addAnnotaion:tempAnnot with:pinAnnotationView];
            break;
            
        case ANNOTATION_BILLBOARD: {//添加布告牌
            UIImage *pinImage = [UIImage imageNamed:@"res_bMap/billboard.png"];
            pinAnnotationView.image = pinImage;
            pinAnnotationView.centerOffset = CGPointMake(0, -pinAnnotationView.bounds.size.height/2);
            [self addBillboard:tempAnnot with:pinAnnotationView];
            //添加单击事件
            UIButton *tap = [UIButton buttonWithType:UIButtonTypeCustom];
            tap.frame = pinAnnotationView.bounds;
            tap.tag= tempAnnot.annoId;
            [tap addTarget:self action:@selector(selectBillboard:) forControlEvents:UIControlEventTouchDown];
            [pinAnnotationView addSubview:tap];
        }
            break;
            
        case ANNOTATION_MOBILE: {//可移动的标注
            UIImage *pinImage = [UIImage imageNamed:@"res_bMap/mobile.png"];
            pinAnnotationView.image = pinImage;
            NSString *mobilePath = tempAnnot.pinImg;
            UIImage *mbileImage = [UIImage imageWithContentsOfFile:[self getPathWithUZSchemeURL:mobilePath]];
            CGRect mobileRect = CGRectMake(0, 0, mbileImage.size.width/2.0, mbileImage.size.height/2.0);
            UIImageView *mobileIcon = [[UIImageView alloc]init];
            mobileIcon.frame = mobileRect;
            mobileIcon.image = mbileImage;
            [pinAnnotationView addSubview:mobileIcon];
            mobileIcon.center = CGPointMake(pinAnnotationView.bounds.size.width/2.0, pinAnnotationView.bounds.size.height/2.0);
            mobileIcon.tag = 986;
            pinAnnotationView.centerOffset = CGPointMake(0, 0);
        }
            break;
            
        default:
            [self addAnnotaion:tempAnnot with:pinAnnotationView];
            break;
    }
    return pinAnnotationView;
}

- (void)mapViewFitPolyLine:(BMKPolyline *) polyLine {//根据polyline设置地图范围
    CGFloat ltX, ltY, rbX, rbY;
    if (polyLine.pointCount < 1) {
        return;
    }
    BMKMapPoint pt = polyLine.points[0];
    ltX = pt.x, ltY = pt.y;
    rbX = pt.x, rbY = pt.y;
    for (int i = 1; i < polyLine.pointCount; i++) {
        BMKMapPoint pt = polyLine.points[i];
        if (pt.x < ltX) {
            ltX = pt.x;
        }
        if (pt.x > rbX) {
            rbX = pt.x;
        }
        if (pt.y > ltY) {
            ltY = pt.y;
        }
        if (pt.y < rbY) {
            rbY = pt.y;
        }
    }
    BMKMapRect rect;
    rect.origin = BMKMapPointMake(ltX , ltY);
    rect.size = BMKMapSizeMake(rbX - ltX, rbY - ltY);
    [_baiduMapView setVisibleMapRect:rect];
    _baiduMapView.zoomLevel = _baiduMapView.zoomLevel - 0.3;
}

- (UIImage *)imageRotated:(UIImage *)image ByDegrees:(CGFloat)degrees {//图片旋转
    CGFloat width = CGImageGetWidth(image.CGImage);
    CGFloat height = CGImageGetHeight(image.CGImage);
    CGSize rotatedSize;
    rotatedSize.width = width;
    rotatedSize.height = height;
    UIGraphicsBeginImageContext(rotatedSize);
    CGContextRef bitmap = UIGraphicsGetCurrentContext();
    CGContextTranslateCTM(bitmap, rotatedSize.width/2, rotatedSize.height/2);
    CGContextRotateCTM(bitmap, degrees * M_PI / 180);
    CGContextRotateCTM(bitmap, M_PI);
    CGContextScaleCTM(bitmap, -1.0, 1.0);
    CGContextDrawImage(bitmap, CGRectMake(-rotatedSize.width/2, -rotatedSize.height/2, rotatedSize.width, rotatedSize.height), image.CGImage);
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

- (void)addWalkingRoute:(BMKWalkingRouteLine *)plan withStartIcon:(NSString *)startIcon andEndIcon:(NSString *)endIcon idStr:(NSString *)routeId fit:(BOOL)autofit {//添加步行路线
    NSInteger size = [plan.steps count];
    int planPointCounts = 0;
    NSMutableArray *allRouteNode = [NSMutableArray arrayWithCapacity:1];
    for (int i = 0; i < size; i++) {
        BMKWalkingStep *transitStep = [plan.steps objectAtIndex:i];
        if(i == 0) {
            UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
            item.coordinate = plan.starting.location;
            item.title = plan.starting.title;
            item.classify = ANNOTATION_ROUTE_START;
            item.clikType = ANNOTATION_CLICK_POINT;
            item.pinImg = startIcon;
            [_baiduMapView addAnnotation:item]; // 添加起点标注
            [allRouteNode addObject:item];
        } else if(i == size-1) {
            UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
            item.coordinate = plan.terminal.location;
            item.title = plan.terminal.title;;
            item.classify = ANNOTATION_ROUTE_END;
            item.clikType = ANNOTATION_CLICK_POINT;
            item.pinImg = endIcon;
            [_baiduMapView addAnnotation:item]; // 添加起点标注
            [allRouteNode addObject:item];
        }
        //添加annotation节点
        UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
        item.coordinate = transitStep.entrace.location;
        item.title = transitStep.entraceInstruction;
        item.degree = transitStep.direction * 30;
        item.classify = ANNOTATION_ROUTE_NODE;
        item.clikType = ANNOTATION_CLICK_ROUTE;
        item.nodeIndex = i;
        item.routeLineId = routeId;
        [_baiduMapView addAnnotation:item];
        [allRouteNode addObject:item];
        //轨迹点总数累计
        planPointCounts += transitStep.pointsCount;
    }
    //轨迹点
    BMKMapPoint *temppoints = new BMKMapPoint[planPointCounts];
    int i = 0;
    for (int j = 0; j < size; j++) {
        BMKWalkingStep *transitStep = [plan.steps objectAtIndex:j];
        int k=0;
        for(k=0;k<transitStep.pointsCount;k++) {
            temppoints[i].x = transitStep.points[k].x;
            temppoints[i].y = transitStep.points[k].y;
            i++;
        }
    }
    // 通过points构建BMKPolyline
    UZBMKPolyline *polyLine = [[UZBMKPolyline alloc]init];
    [polyLine setPolylineWithPoints:temppoints count:planPointCounts];
    polyLine.lineType = 1;
    [_baiduMapView addOverlay:polyLine];
    delete []temppoints;
    if (autofit) {
        [self mapViewFitPolyLine:polyLine];
    }
    [self.allRoutes setObject:polyLine forKey:routeId];
    [self.routeNodeSet setObject:allRouteNode forKey:routeId];
}

- (void)addDriveRoute:(BMKDrivingRouteLine *)plan withStartIcon:(NSString *)startIcon andEndIcon:(NSString *)endIcon idStr:(NSString *)routeId fit:(BOOL)autofit {// 添加驾车路线
    NSInteger size = [plan.steps count];
    int planPointCounts = 0;
    NSMutableArray *allRouteNode = [NSMutableArray arrayWithCapacity:1];
    for (int i = 0; i < size; i++) {
        BMKDrivingStep *transitStep = [plan.steps objectAtIndex:i];
        if(i == 0) {
            UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
            item.coordinate = plan.starting.location;
            item.title = plan.starting.title;
            item.classify = ANNOTATION_ROUTE_START;
            item.pinImg = startIcon;
            item.clikType = ANNOTATION_CLICK_POINT;
            [_baiduMapView addAnnotation:item]; // 添加起点标注
            [allRouteNode addObject:item];
        } else if(i == size-1) {
            UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
            item.coordinate = plan.terminal.location;
            item.title = plan.terminal.title;;
            item.classify = ANNOTATION_ROUTE_END;
            item.pinImg = endIcon;
            item.clikType = ANNOTATION_CLICK_POINT;
            [_baiduMapView addAnnotation:item]; // 添加终点标注
            [allRouteNode addObject:item];
        }
        //添加annotation节点
        UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
        item.coordinate = transitStep.entrace.location;
        item.title = transitStep.entraceInstruction;
        item.degree = transitStep.direction * 30;
        item.classify = ANNOTATION_ROUTE_NODE;
        item.clikType = ANNOTATION_CLICK_ROUTE;
        item.nodeIndex = i;
        item.routeLineId = routeId;
        [_baiduMapView addAnnotation:item];
        [allRouteNode addObject:item];
        //轨迹点总数累计
        planPointCounts += transitStep.pointsCount;
    }
    //轨迹点
    BMKMapPoint *temppoints = new BMKMapPoint[planPointCounts];
    int i = 0;
    for (int j = 0; j < size; j++) {
        BMKDrivingStep *transitStep = [plan.steps objectAtIndex:j];
        int k=0;
        for(k=0;k<transitStep.pointsCount;k++) {
            temppoints[i].x = transitStep.points[k].x;
            temppoints[i].y = transitStep.points[k].y;
            i++;
        }
    }
    // 通过points构建BMKPolyline
    UZBMKPolyline *polyLine = [[UZBMKPolyline alloc]init];
    [polyLine setPolylineWithPoints:temppoints count:planPointCounts];
    polyLine.lineType = 1;
    [_baiduMapView addOverlay:polyLine];
    delete []temppoints;
    if (autofit) {
        [self mapViewFitPolyLine:polyLine];
    }
    [self.allRoutes setObject:polyLine forKey:routeId];
    [self.routeNodeSet setObject:allRouteNode forKey:routeId];
}

- (void)addTransitRoute:(BMKTransitRouteLine *)plan withStartIcon:(NSString *)startIcon andEndIcon:(NSString *)endIcon idStr:(NSString *)routeId fit:(BOOL)autofit {//添加公交路线
    NSInteger size = [plan.steps count];
    int planPointCounts = 0;
    NSMutableArray *allRouteNode = [NSMutableArray arrayWithCapacity:1];
    for (int i = 0; i < size; i++) {
        BMKTransitStep *transitStep = [plan.steps objectAtIndex:i];
        if(i == 0) {
            UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
            item.coordinate = plan.starting.location;
            item.title = plan.starting.title;
            item.classify = ANNOTATION_ROUTE_START;
            item.clikType = ANNOTATION_CLICK_POINT;
            item.pinImg = startIcon;
            [_baiduMapView addAnnotation:item]; // 添加起点标注
            [allRouteNode addObject:item];
        } else if(i == size-1) {
            UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
            item.coordinate = plan.terminal.location;
            item.title = plan.terminal.title;;
            item.classify = ANNOTATION_ROUTE_END;
            item.clikType = ANNOTATION_CLICK_POINT;
            item.pinImg = endIcon;
            [_baiduMapView addAnnotation:item]; // 添加起点标注
            [allRouteNode addObject:item];
        }
        //添加annotation节点
        UZbMapAnnotation *item = [[UZbMapAnnotation alloc]init];
        item.coordinate = transitStep.entrace.location;
        item.title = transitStep.instruction;
        if (transitStep.stepType == BMK_BUSLINE) {
            item.classify = ANNOTATION_ROUTE_BUS;
        } else if (transitStep.stepType == BMK_SUBWAY) {
            item.classify = ANNOTATION_ROUTE_RAIL;
        } else if (transitStep.stepType == BMK_WAKLING) {
            item.classify = ANNOTATION_ROUTE_NODE;
        } else {
            item.classify = ANNOTATION_ROUTE_BUS;
        }
        item.nodeIndex = i;
        item.routeLineId = routeId;
        item.clikType = ANNOTATION_CLICK_ROUTE;
        [_baiduMapView addAnnotation:item];
        [allRouteNode addObject:item];
        //轨迹点总数累计
        planPointCounts += transitStep.pointsCount;
    }
    //轨迹点
    BMKMapPoint *temppoints = new BMKMapPoint[planPointCounts];
    int i = 0;
    for (int j = 0; j < size; j++) {
        BMKTransitStep *transitStep = [plan.steps objectAtIndex:j];
        int k=0;
        for(k=0;k<transitStep.pointsCount;k++) {
            temppoints[i].x = transitStep.points[k].x;
            temppoints[i].y = transitStep.points[k].y;
            i++;
        }
    }
    // 通过points构建BMKPolyline
    UZBMKPolyline *polyLine = [[UZBMKPolyline alloc]init];
    [polyLine setPolylineWithPoints:temppoints count:planPointCounts];
    polyLine.lineType = 1;
    [_baiduMapView addOverlay:polyLine];
    delete []temppoints;
    if (autofit) {
        [self mapViewFitPolyLine:polyLine];
    }
    [self.allRoutes setObject:polyLine forKey:routeId];
    [self.routeNodeSet setObject:allRouteNode forKey:routeId];
}

- (void)searchBusRoute:(UZBMKPoiSearch *)tempSearcher withResult:(BMKPoiResult *)result andErroCode:(BMKSearchErrorCode)errorCode {//根据poi结果搜索公交路线
    if (errorCode == BMK_SEARCH_NO_ERROR) {
        NSMutableDictionary *cbDict = [NSMutableDictionary dictionaryWithCapacity:1];
        NSMutableArray *dictAry = [NSMutableArray arrayWithCapacity:1];
        for (int i = 0; i < result.poiInfoList.count; i++){
            NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:1];
            BMKPoiInfo *poi = [result.poiInfoList objectAtIndex:i];
            if (poi.epoitype == 2 || poi.epoitype == 4) {
                NSString *name = [NSString stringWithFormat:@"%@",poi.name];
                NSString *uid = [NSString stringWithFormat:@"%@",poi.uid];
                NSString *city = [NSString stringWithFormat:@"%@",poi.city];
                if (name && name.length>0) {
                    [dict setObject:name forKey:@"name"];
                }
                if (uid && uid.length>0) {
                    [dict setObject:uid forKey:@"uid"];
                }
                if (city && city.length>0) {
                    [dict setObject:city forKey:@"city"];
                }
                [dict setObject:[NSNumber numberWithInt:poi.epoitype] forKey:@"poiType"];//POI类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路
                [dictAry addObject:dict];
            }
        }
        [cbDict setObject:dictAry forKey:@"results"];
        [cbDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [self sendResultEventWithCallbackId:getBusRouteCbid dataDict:cbDict errDict:nil  doDelete:YES];
    } else {
        int errCode = 0;
        switch (errorCode) {
            case BMK_SEARCH_AMBIGUOUS_KEYWORD:
                errCode = 1;
                break;
                
            case BMK_SEARCH_AMBIGUOUS_ROURE_ADDR:
                errCode = 2;
                break;
                
            case BMK_SEARCH_RESULT_NOT_FOUND:
                errCode = 3;
                break;
                
            case BMK_SEARCH_KEY_ERROR:
                errCode = 4;
                break;
                
            case BMK_SEARCH_NETWOKR_ERROR:
                errCode = 5;
                break;
                
            case BMK_SEARCH_NETWOKR_TIMEOUT:
                errCode = 6;
                break;
                
            case BMK_SEARCH_PERMISSION_UNFINISHED:
                errCode = 7;
                break;
                
            default:
                break;
        }
        [self sendResultEventWithCallbackId:getBusRouteCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:errCode] forKey:@"code"]  doDelete:YES];
    }
}

- (void)searchInCityRoute:(UZBMKPoiSearch *)tempSearcher withResult:(BMKPoiResult *)result andErroCode:(BMKSearchErrorCode)errorCode {//根据poi结果搜索公交路线
    NSInteger searchCbid;
    switch (tempSearcher.type) {
            
        case UZSEARCH_INCITY:
            searchCbid = searchIncityCbid;
            break;
            
        case UZSEARCH_NEARBY:
            searchCbid = searchNearByCbid;
            break;
            
        case UZSEARCH_INBOUNDS:
            searchCbid = searchInboundsCbid;
            break;
            
        default:
            searchCbid = searchIncityCbid;
            break;
    }
    if (errorCode == BMK_SEARCH_NO_ERROR) {
        NSMutableDictionary *cbDict = [NSMutableDictionary dictionaryWithCapacity:1];
        NSMutableArray *dictAry = [NSMutableArray arrayWithCapacity:1];
        for (int i = 0; i < result.poiInfoList.count; i++){
            NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:1];
            BMKPoiInfo* poi = [result.poiInfoList objectAtIndex:i];
            NSString *name = [NSString stringWithFormat:@"%@",poi.name];
            NSString *uid = [NSString stringWithFormat:@"%@",poi.uid];
            NSString *address = [NSString stringWithFormat:@"%@",poi.address];
            NSString *city = [NSString stringWithFormat:@"%@",poi.city];
            NSString *phone = [NSString stringWithFormat:@"%@",poi.phone];
            [dict setObject:[NSNumber numberWithDouble:poi.pt.longitude] forKey:@"lon"];
            [dict setObject:[NSNumber numberWithDouble:poi.pt.latitude] forKey:@"lat"];
            if (name && name.length>0) {
                [dict setObject:name forKey:@"name"];
            }
            if (uid && uid.length>0) {
                [dict setObject:uid forKey:@"uid"];
            }
            if (address && address.length>0) {
                [dict setObject:address forKey:@"address"];
            }
            if (city && city.length>0) {
                [dict setObject:city forKey:@"city"];
            }
            if (phone && phone.length>0) {
                [dict setObject:phone forKey:@"phone"];
            }
            [dict setObject:[NSNumber numberWithInt:poi.epoitype] forKey:@"poiType"];//POI类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路
            [dictAry addObject:dict];
        }
        [cbDict setObject:[NSNumber numberWithInt:result.totalPoiNum] forKey:@"totalNum"];
        [cbDict setObject:[NSNumber numberWithInt:result.currPoiNum] forKey:@"currentNum"];
        [cbDict setObject:[NSNumber numberWithInt:result.pageNum] forKey:@"totalPage"];
        [cbDict setObject:[NSNumber numberWithInt:result.pageIndex] forKey:@"pageIndex"];
        [cbDict setObject:dictAry forKey:@"results"];
        [cbDict setObject:[NSNumber numberWithBool:YES] forKey:@"status"];
        [self sendResultEventWithCallbackId:searchCbid dataDict:cbDict errDict:nil  doDelete:YES];
    } else {
        int errCode = 0;
        switch (errorCode) {
            case BMK_SEARCH_AMBIGUOUS_KEYWORD:
                errCode = 1;
                break;
                
            case BMK_SEARCH_AMBIGUOUS_ROURE_ADDR:
                errCode = 2;
                break;
                
            case BMK_SEARCH_RESULT_NOT_FOUND:
                errCode = 3;
                break;
                
            case BMK_SEARCH_KEY_ERROR:
                errCode = 4;
                break;
                
            case BMK_SEARCH_NETWOKR_ERROR:
                errCode = 5;
                break;
                
            case BMK_SEARCH_NETWOKR_TIMEOUT:
                errCode = 6;
                break;
                
            case BMK_SEARCH_PERMISSION_UNFINISHED:
                errCode = 7;
                break;
                
            default:
                break;
        }
        [self sendResultEventWithCallbackId:searchCbid dataDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"status"] errDict:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:errCode] forKey:@"code"]  doDelete:YES];
    }
}

- (void)showCurrentLocation:(BMKUserLocation *)userLocation {
    if (openShow && openSetCenter) {
        return;
    }
    if (showCurrentUserLoc) {
        CLLocation *newLocation = userLocation.location;
        double lat = newLocation.coordinate.latitude;
        double lon = newLocation.coordinate.longitude;
        CLLocationCoordinate2D location2D ;
        location2D.longitude = lon;
        location2D.latitude = lat;
        [_baiduMapView setCenterCoordinate:location2D animated:YES];
        showCurrentUserLoc = NO;
    }
}

- (void)getCurrentLocation:(BMKUserLocation *)userLocation {
    CLLocation *newLocation = userLocation.location;
    double lat = newLocation.coordinate.latitude;
    double lon = newLocation.coordinate.longitude;
    if ([self isValidLon:lon lat:lat]) {
        long long timestamp = (long long)([newLocation.timestamp timeIntervalSince1970] * 1000);
        NSMutableDictionary *info = [NSMutableDictionary dictionary];
        [info setObject:@(YES) forKey:@"status"];
        [info setObject:[NSNumber numberWithDouble:lat] forKey:@"lat"];
        [info setObject:[NSNumber numberWithDouble:lon] forKey:@"lon"];
        [info setObject:@(timestamp) forKey:@"timestamp"];
        if (startLocationCbid >= 0) {
            [self sendResultEventWithCallbackId:startLocationCbid dataDict:info errDict:nil doDelete:shouldAutoStop];
        }
    } else {
        shouldAutoStop = YES;
        NSDictionary *ret = @{@"status":@(NO)};
        NSDictionary *err = @{@"code":[NSNumber numberWithInt:-1],@"msg":@"location failed"};
        [self sendResultEventWithCallbackId:startLocationCbid dataDict:ret errDict:err doDelete:YES];
    }
    if (shouldAutoStop) {
        locationStarted = NO;
        startLocationCbid = -1;
    }
}

@end
