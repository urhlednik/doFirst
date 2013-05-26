//
//  NetworkClient.m
//  guestking
//
//  Created by Jungsuk Song on 12. 12. 2..
//  Copyright (c) 2012년 Jungsuk Song. All rights reserved.
//

#import "NetworkClient.h"
#import "HTTPRequest.h"
#import "UIDevice+IdentifierAddition.h"

@implementation NetworkClient


- (id)init
{
    self = [super init];
    if (self) {
        _parser = [[SBJsonParser alloc] init];
    }
    
    return self;
}

- (void) requestLogList:(NSDictionary *)conditions
{
    NSString *url = [NSString stringWithFormat:@"%@/update_log",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedResult:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
}

- (void) receivedResult:(id)sender
{
    if (sender)
    {
        NSString *json_string = sender;
    }
}

@end
