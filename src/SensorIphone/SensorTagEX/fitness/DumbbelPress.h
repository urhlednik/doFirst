//
//  DumbbelPress.h
//  SensorTagPS
//
//  Created by Jungsuk Song on 13. 7. 14..
//  Copyright (c) 2013년 Texas Instruments. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Sensors.h"


@interface DumbbelPress : NSObject
{
    NSMutableArray       *m_recentDataList;
    int                  m_beforeMovingDir;
    int                  m_checkPressDir;
    
    int                  m_pressCount;
}

- (int)getDumbbelPressCount:(sensorTagValues *)sensorData;



@end
