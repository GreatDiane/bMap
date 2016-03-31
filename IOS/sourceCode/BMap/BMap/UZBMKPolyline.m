/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import "UZBMKPolyline.h"

@implementation UZBMKPolyline

@synthesize lineType;

- (id)init {
    self = [super init];
    if (self != nil) {
        self.lineType = 0;
    }
    return self;
}

@end
