//
//  HTTPRequest.m
//  guestking
//
//  Created by Jungsuk Song on 12. 11. 23..
//  Copyright (c) 2012년 Jungsuk Song. All rights reserved.
//

#import "HTTPRequest.h"

@implementation HTTPRequest
@synthesize receivedData;
@synthesize response;
@synthesize result;
@synthesize target;
@synthesize selector;

- (BOOL)requestUrl:(NSString *)url bodyObject:(NSDictionary *)bodyObject
{
    // URL Request 객체 생성
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:5.0f];
    
    // 통신방식 정의 (POST, GET)
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    // bodyObject의 객체가 존재할 경우 QueryString형태로 변환
    if(bodyObject)
    {
        // 임시 변수 선언
        NSMutableArray *parts = [NSMutableArray array];
        NSString *part;
        id key;
        id value;
        
        // 값을 하나하나 변환
        for(key in bodyObject)
        {
            value = [bodyObject objectForKey:key];
            part = [NSString stringWithFormat:@"%@=%@", [key stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding],
                    [value stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
            [parts addObject:part];
        }
        
        // 값들을 &로 연결하여 Body에 사용
        [request setHTTPBody:[[parts componentsJoinedByString:@"&"] dataUsingEncoding:NSUTF8StringEncoding]];
        
        NSLog(@"Request Post Body: %@", [parts componentsJoinedByString:@"&"] );
    }
    NSLog(@"Request Post: %@", url);

    
    // Request를 사용하여 실제 연결을 시도하는 NSURLConnection 인스턴스 생성
#if !__has_feature(objc_arc)
    NSURLConnection *connection = [[[NSURLConnection alloc] initWithRequest:request delegate:self] autorelease];

#else
    NSURLConnection *connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
#endif
    
    // 정상적으로 연결이 되었다면
    if(connection)
    {
        // 데이터를 전송받을 멤버 변수 초기화
        receivedData = [[NSMutableData alloc] init];
        return YES;
    }
    
    return NO;
}

- (BOOL)requestUrl:(NSString *)url bodyObject:(NSDictionary *)bodyObject attachPhoto:(UIImage *)image photoName:(NSString *)photo_name
{
    // URL Request 객체 생성
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:5.0f];
    
    // 통신방식 정의 (POST, GET)
    [request setHTTPMethod:@"POST"];
    [request setCachePolicy:NSURLRequestUseProtocolCachePolicy];
    NSString *boundary = @"AaB03x";
    NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@",boundary];
    [request addValue:contentType forHTTPHeaderField: @"Content-Type"];
    
    
    // bodyObject의 객체가 존재할 경우 QueryString형태로 변환
    if(bodyObject)
    {
        NSMutableData *body = [NSMutableData data];
        for(NSString* key in [bodyObject allKeys]) {
            [self appendDataForHTTPPOST:body boundary:boundary addkey:key withValue:[bodyObject objectForKey:key]];
        }
        
        [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];
        
        [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"photo\"; filename=\"%@\"\r\n",photo_name] dataUsingEncoding:NSUTF8StringEncoding]];
        
        [body appendData:[@"Content-Type: application/octet-stream\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:UIImageJPEGRepresentation(image,1.0)];  //[NSData dataWithData:image]];
        
        [body appendData:[[NSString stringWithFormat:@"\r\n--%@--\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];

        // setting the body of the post to the reqeust
        [request setHTTPBody:body];
    }
    NSLog(@"Request Post: %@", url);
    
    
    // Request를 사용하여 실제 연결을 시도하는 NSURLConnection 인스턴스 생성
#if !__has_feature(objc_arc)
    NSURLConnection *connection = [[[NSURLConnection alloc] initWithRequest:request delegate:self] autorelease];
    
#else
    NSURLConnection *connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
#endif
    
    // 정상적으로 연결이 되었다면
    if(connection)
    {
        // 데이터를 전송받을 멤버 변수 초기화
        receivedData = [[NSMutableData alloc] init];
        return YES;
    }
    
    return NO;
}

-(void) appendDataForHTTPPOST:(NSMutableData*) mutableData boundary:(NSString*) boundary addkey:(NSString*) key withValue:(NSString*) value{
    [mutableData appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    [mutableData appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n%@", key, value] dataUsingEncoding:NSUTF8StringEncoding]];
}

- (BOOL)requestUrl:(NSString *)url
{
    // URL Request 객체 생성
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:20.0f];
    
    // 통신방식 정의 (POST, GET)
    [request setHTTPMethod:@"GET"];

    NSLog(@"Request Get: %@", url);
    
    // Request를 사용하여 실제 연결을 시도하는 NSURLConnection 인스턴스 생성
#if !__has_feature(objc_arc)
    NSURLConnection *connection = [[[NSURLConnection alloc] initWithRequest:request delegate:self] autorelease];
    
#else
    NSURLConnection *connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
#endif
    
    // 정상적으로 연결이 되었다면
    if(connection)
    {
        // 데이터를 전송받을 멤버 변수 초기화
        receivedData = [[NSMutableData alloc] init];
        return YES;
    }
    
    return NO;
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)aResponse
{
    // 데이터를 전송받기 전에 호출되는 메서드, 우선 Response의 헤더만을 먼저 받아 온다.
    //[receivedData setLength:0];
    self.response = aResponse;
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    // 데이터를 전송받는 도중에 호출되는 메서드, 여러번에 나누어 호출될 수 있으므로 appendData를 사용한다.
    [receivedData appendData:data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    // 에러가 발생되었을 경우 호출되는 메서드
    NSLog(@"Error: %@", [error localizedDescription]);
    if(target)
    {
        [target performSelector:selector withObject:nil];
    }
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    // 데이터 전송이 끝났을 때 호출되는 메서드, 전송받은 데이터를 NSString형태로 변환한다.
    result = [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding];
    
    // 델리게이트가 설정되어있다면 실행한다.
    if(target)
    {
        [target performSelector:selector withObject:result];
    }
}

- (void)setDelegate:(id)aTarget selector:(SEL)aSelector
{
    // 데이터 수신이 완료된 이후에 호출될 메서드의 정보를 담고 있는 셀렉터 설정
    self.target = aTarget;
    self.selector = aSelector;
}

- (void)dealloc
{
#if !__has_feature(objc_arc)   
    [receivedData release];
    [response release];
    [result release];
    [super dealloc];
#endif
    
}

@end
