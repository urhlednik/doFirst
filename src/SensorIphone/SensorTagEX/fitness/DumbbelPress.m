//
//  DumbbelPress.m
//  SensorTagPS
//
//  Created by Jungsuk Song on 13. 7. 14..
//  Copyright (c) 2013년 Texas Instruments. All rights reserved.
//

#import "DumbbelPress.h"

@implementation DumbbelPress

- (int)getDumbbelPressCount:(sensorTagValues *)sensorData
{
    
    [m_recentDataList addObject:sensorData];
    if (m_recentDataList.count > 10)
    {
        [m_recentDataList removeObjectAtIndex:0];
    }
    
    const int currMovingDir = abs(sensorData.accZ);
    if (m_checkPressDir == 0 && currMovingDir != 0) m_checkPressDir = currMovingDir;
    if (currMovingDir != m_beforeMovingDir)
    {
        if (currMovingDir == m_checkPressDir) m_pressCount++;
    }
    
    return m_pressCount;
}


@end
