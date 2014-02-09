//
//  DumbbelPressResult.h
//  SensorTagPS
//
//  Created by Jungsuk Song on 13. 7. 14..
//  Copyright (c) 2013년 Texas Instruments. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DumbbelPressResult : NSObject

@property (nonatomic) int DumbbelCount;
@property NSString *TimeStamp;              // 마지막에 했는 시간

@end
