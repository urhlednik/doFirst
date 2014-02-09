//
//  FitnessManager.h
//  SensorTagPS
//
//  Created by Jungsuk Song on 13. 7. 14..
//  Copyright (c) 2013년 Texas Instruments. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DumbbelPress.h"
#import "Sensors.h"

@interface FitnessManager : NSObject
{
    
}

- (DumbbelPress *)calcDumbbelCount:(NSArray *)sensorDataList;

@end
