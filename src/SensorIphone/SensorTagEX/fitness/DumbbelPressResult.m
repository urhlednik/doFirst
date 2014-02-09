//
//  DumbbelPressResult.m
//  SensorTagPS
//
//  Created by Jungsuk Song on 13. 7. 14..
//  Copyright (c) 2013년 Texas Instruments. All rights reserved.
//

#import "DumbbelPressResult.h"

@implementation DumbbelPressResult

-(id) init {
    self = [super init];
    if (self) {
        self.DumbbelCount = 0;
        self.TimeStamp = @"";
    }
    return self;
}


@end
