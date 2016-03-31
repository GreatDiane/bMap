/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import "UZBMKPoiSearch.h"

@implementation UZBMKPoiSearch

@synthesize isUplink, routId;
@synthesize city;
@synthesize type;

- (id)init {
    self = [super init];
    if (self != nil) {
        self.type = UZSEARCH_BUS;
    }
    return self;
}
@end
