//
//  UZBMKDistrictSearch.h
//  BMap
//
//  Created by 郑连乐 on 2019/2/22.
//  Copyright © 2019年 apicloud. All rights reserved.
//

#import <BaiduMapAPI_Search/BMKSearchComponent.h>

@interface UZBMKDistrictSearch : BMKDistrictSearch

@property (nonatomic, strong) NSString * districtId;
@property (nonatomic, strong) NSDictionary * style;

@end


