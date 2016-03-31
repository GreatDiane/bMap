/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

//#import <BaiduMapAPI/BMapKit.h>
#import <BaiduMapAPI_Search/BMKSearchComponent.h>

typedef enum{
    UZSEARCH_BUS = 0,   //搜公交地铁路线
    UZSEARCH_INCITY,    //在城市里搜关键字
    UZSEARCH_NEARBY,    //在附近搜关键字
    UZSEARCH_INBOUNDS   //在指定区域搜关键字
} SEARCHTYPE;

@interface UZBMKPoiSearch : BMKPoiSearch

@property (nonatomic, strong) NSString *city;
@property (nonatomic, assign) BOOL isUplink;
@property (nonatomic, strong) NSString *routId;
@property (nonatomic, assign) SEARCHTYPE type;

@end
