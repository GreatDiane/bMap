/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import "UZbMapAnnotation.h"

static double interpolate(double from, double to, NSTimeInterval time) {
    return (to - from) * time + from;
}

static CLLocationDegrees interpolateDegrees(CLLocationDegrees from, CLLocationDegrees to, NSTimeInterval time) {
    return interpolate(from, to, time);
}

static CLLocationCoordinate2D interpolateCoordinate(CLLocationCoordinate2D from, CLLocationCoordinate2D to, NSTimeInterval time) {
    return CLLocationCoordinate2DMake(interpolateDegrees(from.latitude, to.latitude, time), interpolateDegrees(from.longitude, to.longitude, time));
}

@implementation UZbMapAnnotation

@synthesize pinImg, bubbleBgImg;
@synthesize coordinate;
@synthesize annoId, nodeIndex;
@synthesize title, subtitle;
@synthesize isStyled, draggable, popBubble;
@synthesize styles, content;
@synthesize type;
@synthesize classify = _classify;
@synthesize degree = _degree;
@synthesize clikType;
@synthesize routeLineId;
@synthesize currentAngle, moveDuration;
@synthesize toCoords, fromCoords;
@synthesize lastStep, timeOffset;
@synthesize delegate;

- (id)initWithCoordinate:(CLLocationCoordinate2D)newCoordinate{
    self = [super init];
    if (self) {
        coordinate = newCoordinate;
        self.classify = ANNOTATION;
    }
    return  self;
}

- (id)init {
    self = [super init];
    if (self) {
        self.classify = ANNOTATION;
        self.clikType = ANNOTATION_CLICK;
        self.popBubble = NO;
    }
    return  self;
}

- (void)moveStep {
    CFTimeInterval thisStep = CACurrentMediaTime();
    CFTimeInterval stepDuration = thisStep - self.lastStep;
    self.lastStep = thisStep;
    self.timeOffset = MIN(self.timeOffset + stepDuration, moveDuration);
    NSTimeInterval time = self.timeOffset / self.moveDuration;
    CLLocationCoordinate2D coords = interpolateCoordinate(fromCoords, toCoords, time);
    [self setCoordinate:coords];
    if (self.timeOffset >= moveDuration) {
        self.timeOffset = 0;
        if ([self.delegate respondsToSelector:@selector(didMoving:)]) {
            [self.delegate didMoving:self];
        }
    }
}
@end
