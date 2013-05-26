//
//  NetworkClient.h
//  guestking
//
//  Created by Jungsuk Song on 12. 12. 2..
//  Copyright (c) 2012년 Jungsuk Song. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SBJson.h"

#define SERVER_ADDR @"127.0.0.1"

@interface NetworkClient : NSObject
{
    NSDateFormatter *m_dateFormatter;
    SBJsonParser    *_parser;
}

- (void) requestLogList:(NSDictionary *)conditions;

@end
