/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import <Foundation/Foundation.h>
//#import <BaiduMapAPI/BMapKit.h>
#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import <BaiduMapAPI_Base/BMKBaseComponent.h>

typedef enum {
    ANNOTATION_MARKE = 0,  //标注
    ANNOTATION_BILLBOARD,  //布告牌
    ANNOTATION_MOBILE      //可移动的标注
} AnnotationType;

typedef enum {
    ANNOTATION = 0,        //标注、布告牌、可移动的标注
    ANNOTATION_ROUTE_START,//路线起点
    ANNOTATION_ROUTE_END,  //路线终点
    ANNOTATION_ROUTE_NODE, //步行、驾车路线结点
    ANNOTATION_ROUTE_BUS,  //公交路线结点
    ANNOTATION_ROUTE_RAIL  //地铁路线结点
} AnnotationClassify;


typedef enum {
    ANNOTATION_CLICK = 0,        //点击标注、布告牌
    ANNOTATION_CLICK_POINT,      //点击路线起点，终点
    ANNOTATION_CLICK_ROUTE,      //点击路线结点
    ANNOTATION_CLICK_BUS_ROUTE,  //点击公交路线的结点
    ANNOTATION_CLICK_MOBILEANNO  //点击移动的标注事件
} AnnotationClickClassify;

@protocol MovingAnimationDelegate;

@interface UZbMapAnnotation : NSObject
<BMKAnnotation> {
    AnnotationClassify _classify; 
    NSInteger _degree;
}

@property (nonatomic) AnnotationClickClassify clikType;           //annotation的点击事件类型
@property (nonatomic) AnnotationClassify classify;                //annotation的分类
@property (nonatomic) NSInteger degree;                           //annotation的图片旋转角度
@property (nonatomic, assign) CLLocationCoordinate2D coordinate;  //annotation的位置坐标
@property (nonatomic, strong) NSString *pinImg, *bubbleBgImg;     //annotation的图片和气泡背景图片
@property (nonatomic, assign) NSInteger annoId, nodeIndex, clickCbId;        //annotation的id
@property (nonatomic, strong) NSString *title, *subtitle;         //annotation的标题和子标题
@property (nonatomic, assign) BOOL isStyled, draggable, popBubble;//annotation是否自定义，是否可拖动
@property (nonatomic, strong) NSDictionary *content, *styles;     //annotation自定义时的内容和样式
@property (nonatomic, assign) AnnotationType type;                //annotation的类型
@property (nonatomic, strong) NSString *routeLineId;              //annotation所在路线的 id
@property (nonatomic, assign) float currentAngle;                 //annotation的图标当前旋转角度（与x轴正向之间的角度）
@property (nonatomic, assign) float moveDuration;                 //annotation的图标移动时间
@property (nonatomic, assign) CLLocationCoordinate2D toCoords, fromCoords;
@property (nonatomic, assign) CFTimeInterval lastStep;
@property (nonatomic, assign) NSTimeInterval timeOffset;
@property (nonatomic, assign) NSInteger bubbleClickCbid;
@property (nonatomic, assign) id <MovingAnimationDelegate> delegate;

- (void)moveStep;

@end

@protocol MovingAnimationDelegate <NSObject>

@optional
- (void)willMoving:(UZbMapAnnotation *)anno;
- (void)didMoving:(UZbMapAnnotation *)anno;

@end
