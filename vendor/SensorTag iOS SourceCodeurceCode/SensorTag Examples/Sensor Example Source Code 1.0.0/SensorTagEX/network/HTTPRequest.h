//
//  HTTPRequest.h
//  guestking
//
//  Created by Jungsuk Song on 12. 11. 23..
//  Copyright (c) 2012년 Jungsuk Song. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HTTPRequest : NSObject
{
    NSMutableData *receivedData;
    NSURLResponse *response;
    NSString *result;
    __weak id target;
    SEL selector;
    
}

- (BOOL)requestUrl:(NSString *)url;
- (BOOL)requestUrl:(NSString *)url bodyObject:(NSDictionary *)bodyObject;
- (BOOL)requestUrl:(NSString *)url bodyObject:(NSDictionary *)bodyObject attachPhoto:(UIImage *)image photoName:(NSString *)photo_name;

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)aResponse;
- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data;
- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error;
- (void)connectionDidFinishLoading:(NSURLConnection *)connection;
- (void)setDelegate:(id)aTarget selector:(SEL)aSelector;

#if !__has_feature(objc_arc)
@property (nonatomic, retain) NSMutableData *receivedData;
@property (nonatomic, retain) NSURLResponse *response;
@property (nonatomic, assign) NSString *result;
@property (nonatomic, assign) id target;
@property (nonatomic, assign) SEL selector;
#else
@property (nonatomic, strong) NSMutableData *receivedData;
@property (nonatomic, strong) NSURLResponse *response;
@property (nonatomic) NSString *result;
@property (nonatomic, weak) id target;
@property (nonatomic) SEL selector;
#endif
@end


/*  사용예
 
 - (void)applicationDidFinishLaunching:(UIApplication *)application {
 // 접속할 주소 설정
 NSString *url = @"http://your.webpage.url";
 
 // HTTP Request 인스턴스 생성
 HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
 
 // POST로 전송할 데이터 설정
 NSDictionary *bodyObject = [NSDictionary dictionaryWithObjectsAndKeys:@"eye",@"name",@"http://theeye.pe.kr", @"home", nil];
 
 // 통신 완료 후 호출할 델리게이트 셀렉터 설정
 [httpRequest setDelegate:self selector:@selector(didReceiveFinished:)];
 
 // 페이지 호출
 [httpRequest requestUrl:url bodyObject:bodyObject];
 
 [window makeKeyAndVisible];
 }

*/