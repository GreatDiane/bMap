/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

//#import <BaiduMapAPI/BMapKit.h>
#import <BaiduMapAPI_Search/BMKSearchComponent.h>

typedef enum {
    SEARCH_NAME = 0, //根据坐标搜名字
    SEARCH_COORD,    //根据名字搜坐标
    SEARCH_ROUTE     //根据坐标搜名字传给searchRoute接口用（仅当type为transit时）
} SearchType;

@interface UZBMKGeoCodeSearch : BMKGeoCodeSearch

@property (nonatomic, assign) SearchType type;
@property (nonatomic, strong) NSString *city;
@property (nonatomic, strong) NSString *address;
@property (nonatomic, strong) NSDictionary *transitInfo;

@end
