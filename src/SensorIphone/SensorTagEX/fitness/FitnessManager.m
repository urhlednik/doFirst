//
//  FitnessManager.m
//  SensorTagPS
//
//  Created by Jungsuk Song on 13. 7. 14..
//  Copyright (c) 2013년 Texas Instruments. All rights reserved.
//

#import "FitnessManager.h"

@implementation FitnessManager
{
    
}

- (DumbbelPress *)calcDumbbelCount:(NSArray *)sensorDataList
{
    // Z 가속도로 판단.
    // +/- 둘중 한쪽이 일정 이상 큰 값이면 반복되고 있으면 해당 방향 지속으로 봄
    // 반대방향이 되고 다시 같은 방향으로 돌아오는 순간이 1회
    
    // up -> down
    
    DumbbelPress *dumbbel = [DumbbelPress new];
    for (sensorTagValues *s in sensorDataList)
    {
        
    }
    
    return dumbbel;
}



@end
