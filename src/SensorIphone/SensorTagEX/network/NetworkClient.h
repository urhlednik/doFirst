//
//  NetworkClient.h
//  guestking
//
//  Created by Jungsuk Song on 12. 12. 2..
//  Copyright (c) 2012년 Jungsuk Song. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MotelInfo.h"
//#import "CouponData.h"
#import "MotelRoomData.h"
#import "ReviewData.h"
#import "PictureData.h"
#import "SBJson.h"

//#define SERVER_ADDR @"http://192.168.10.16:3000"
//#define SERVER_ADDR @"http://admin.guestking.com"
//#define SERVER_ADDR @"http://1.235.94.62:7000"
#define SERVER_ADDR @"http://server.guestking.com"

#define GAE_SVR_ADDR @"http://gk-server.appspot.com"
//#define GAE_SVR_ADDR @"http://192.168.10.16:8082"
//#define GAE_SVR_ADDR @"http://192.168.10.16:8082"

@protocol NetworkMotelResponse

@optional
- (void) responseResultFromServer:(id)msg;
- (void) responseFromServer:(NSArray *)motels;
- (void) responseReviewResult;

- (void) responseCommunityLoad:(id)recv_msg;
- (void) responsePhotoMsg:(id)msg;
- (void) responseBBSMsg:(id)msg;
- (void) responseDirectMsg:(id)msg;

- (void) responseProfile:(id)msg;

@end

@interface NetworkMotelClient : NSObject<NSXMLParserDelegate>
{
    NSXMLParser *xmlParser;
    NSDateFormatter *m_dateFormatter;
    SBJsonParser    *_parser;
    
    NSMutableArray  *m_motelsResult;
    MotelInfo       *m_currParsingMotel;
//    CouponData      *m_currParsingCoupon;
//    MotelRoomData   *m_currParsingRoom;
    ReviewData      *m_currParsingReview;
    PictureData     *m_currParsingPicture;
    NSMutableArray  *m_loadingPictures;
    NSString *m_currElementName;
    NSString *m_currSubElementName;
    float m_currParsingLatitude;
    
    
    UIActivityIndicatorView *m_indicator;
    UIView *m_indicatorBackgroundView;
    //id <NetworkMotelResponse> delegate;
}

- (void) requestMotelsList:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;
- (void) requestMotelsListBySearch:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;
- (void) requestMotelUpdate:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;

- (void) requestPhotos:(NSString *)tags wIndicator:(UIView *)indicatorView;

- (void) requestReviewList:(int)motelId wIndicatorView:(UIView *)indicatorView;
- (void) requestReviewContent:(NSString *)content MotelId:(int)motelId  Score:(int)scorePoint wIndicatorView:(UIView *)indicatorView;
- (void) requestReviewLike:(int)reviewId wIndicatorView:(UIView *)indicatorView;
- (void) requestReviewBad:(int)reviewId wIndicatorView:(UIView *)indicatorView;


- (void) requestLoadCommunity:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView type:(int)msgType;
//- (void) requestLoadTalkMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;
//- (void) requestLoadPhotoMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;
//- (void) requestLoadBBSMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;

- (void) requestCommunityMsg:(NSDictionary *)conditions Type:(int)msgtype attachPhoto:(UIImage*)image photoName:(NSString *)photo_name wIndicatorView:(UIView *)indicatorView;


- (void) requestLoadDirectMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;
- (void) requestUpdateDirectMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;


- (void) requestUpdateProfile:(NSDictionary *)conditions attachPhoto:(UIImage *)image photoName:(NSString *)photo_name wIndicatorView:(UIView *)indicatorView;
- (void) requestLoadProfile:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;
- (void) requestAuthAdult:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView;





/* TODO_JS: 추가 필요
    - 리뷰 등록
 
 */
@property (nonatomic) id <NetworkMotelResponse> delegate;

@end
