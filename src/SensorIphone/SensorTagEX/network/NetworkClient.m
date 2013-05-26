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
#import "CachingMotelData.h"

@implementation NetworkMotelClient


- (id)init
{
    self = [super init];
    if (self) {
        _parser = [[SBJsonParser alloc] init];
    }
    
    return self;
}

- (void) requestMotelsList:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/motels/search.xml",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedMotelsList:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void) requestMotelsListBySearch:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/motels/search_name.xml",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedMotelsList:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
    
}

- (void) requestMotelUpdate:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/motels/update_info.json",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedMotelUpdateResult:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void) requestReviewList:(int)motelId wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/reviews/motel_list.json",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"%d",motelId], @"motel_id", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedReviewList:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void) receivedReviewList:(id)sender
{
    if (sender)
    {
        NSString *json_string = sender;
        
        NSRange range = [json_string rangeOfString:@"{"];
        NSRange range_reverse = [json_string rangeOfString:@"}" options:NSBackwardsSearch];
        
        if (range.location != NSNotFound
            && range_reverse.location != NSNotFound) {
            
            range.length = range_reverse.location - range.location+1;
//            NSString *result = [json_string substringWithRange:range];
            
            NSDateFormatter *dateFormatter = [NSDateFormatter new];
            [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
            
            
            SBJsonParser *parser = [[SBJsonParser alloc] init];
            NSArray *result_reviews = [parser objectWithString:json_string];
            
            int motel_id;
            NSMutableArray *reviews = [NSMutableArray new];
            for (NSDictionary *obj_list in result_reviews)
            {
                
                ReviewData *review = [ReviewData new];
                
                review.ReviewContent = obj_list[@"content"];
                if (obj_list[@"like_count"] != [NSNull null] ) review.LikeCount = [obj_list[@"like_count"] intValue];
                else review.LikeCount = 0;
                if (obj_list[@"bad_count"] != [NSNull null]) review.BadCount = [obj_list[@"bad_count"] intValue];
                else review.BadCount = 0;
                if (obj_list[@"score"]  != [NSNull null]) review.Score = [obj_list[@"score"] intValue];
                else review.Score = 0;
                NSDate *timeDate = [dateFormatter dateFromString:obj_list[@"created_at"]];
                if (timeDate) review.RegistedTime = [timeDate timeIntervalSinceReferenceDate];

                if (obj_list[@"motel_id"]  != [NSNull null]) motel_id = [obj_list[@"motel_id"] intValue];
                else motel_id = 0;
                [reviews addObject:review];
            }
            
            CachingMotelData *cachingData = [CachingMotelData sharedInstance];
            MotelInfo *motel = [[cachingData CachingMotelList] objectForKey:[NSString stringWithFormat:@"%d", motel_id]];
            if (motel)
            {
                motel.Reviews = reviews;
            }
        }
    }
    
    [self stopIndiator];
}


- (void) requestReviewContent:(NSString *)content MotelId:(int)motelId  Score:(int)scorePoint  wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/reviews/post_review.json",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"%d",motelId], @"motel_id",[NSString stringWithFormat:@"%@",[[UIDevice currentDevice] uniqueGlobalDeviceIdentifier]], @"device_id", [NSString stringWithFormat:@"%@",content ],@"content", [NSString stringWithFormat:@"%d",scorePoint], @"score", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedMotelUpdateResult:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void)receivedMotelUpdateResult:(id)sender
{
    [self stopIndiator];
    if (_delegate) [_delegate responseResultFromServer:sender];
}


- (void) requestReviewLike:(int)reviewId wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/reviews/post_like.xml",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"%d",reviewId ],@"review_id", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(responeReviewResult:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void) requestReviewBad:(int)reviewId wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/reviews/post_bad.xml",SERVER_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"%d",reviewId ],@"review_id", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(responeReviewResult:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

// 있어야 할 정보가
//  - 모텔 정보
//      - room 정보
//      - 쿠폰 정보
//      - 리뷰 정보

- (void)receivedMotelsList:(id)sender
{
    CachingMotelData *cachingData = [CachingMotelData sharedInstance];
    [cachingData.FoundMotelList removeAllObjects];
    if (sender == nil)
    {
        [self stopIndiator]; // 애니메이션 중지, 결과가 있는 경우는 파싱이후에 처리
        if (_delegate) [_delegate responseFromServer:nil];
        return;
    }
    
    NSString *result = sender;
    
    NSData *data = [result dataUsingEncoding:NSUTF8StringEncoding];
    
    xmlParser = [[NSXMLParser alloc] initWithData:data];
    xmlParser.delegate = self;
    if([xmlParser parse]){
        NSLog(@"The XML is Parsed.");
        
    }else {
        NSLog(@"Failed to parse the XML");
        [self stopIndiator]; // 애니메이션 중지, 결과가 있는 경우는 파싱이후에 처리
        if (_delegate) [_delegate responseFromServer:nil];
    }
}

- (void)responeReviewResult:(id)sender
{
    [self stopIndiator]; // 애니메이션 중지
    
    [_delegate responseReviewResult];
}

-(void) parserDidStartDocument:(NSXMLParser *)parser{
    NSLog(@"parserDidStartDocument");
    // tab 처리를 위한 전역 변수
    //tabString = [NSMutableString new];
    m_motelsResult = [NSMutableArray new];
    m_loadingPictures = [NSMutableArray new];
}

-(void) parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName
  namespaceURI:(NSString *)namespaceURI
 qualifiedName:(NSString *)qName
    attributes:(NSDictionary *)attributeDict{
    //tabString = [[tabString stringByAppendingString:@"\t"] mutableCopy];
    //NSLog(@"%@", [tabString stringByAppendingString:elementName]);
    m_currElementName = elementName;
    
    if (m_dateFormatter == nil) {
        m_dateFormatter = [[NSDateFormatter alloc] init];
        [m_dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    }
    
    
    if ([elementName isEqualToString:@"motel"])
    {
        m_currParsingMotel = [MotelInfo new];
        [m_motelsResult addObject:m_currParsingMotel];
        m_currSubElementName = @"";
    }
    else if ([elementName isEqualToString:@"pictures"])
    {
        m_currSubElementName = @"pictures";
        
        m_currParsingPicture = [PictureData new];
        //if (m_currParsingMotel.Pictures== nil) m_currParsingMotel.Pictures = [NSMutableArray new];
        m_currParsingPicture.PictureId = -1;
        m_currParsingPicture.ImageId = m_currParsingMotel.ImageId;
        [m_loadingPictures addObject:m_currParsingPicture];
    }
//    else if ([elementName isEqualToString:@"coupons"])
//    {
//        m_currSubElementName = @"coupons";
//        
//        m_currParsingCoupon = [CouponData new];
//        if (m_currParsingMotel.Coupons == nil) m_currParsingMotel.Coupons = [NSMutableArray new];
//        m_currParsingCoupon.CouponUid = -1;
//        [m_currParsingMotel.Coupons addObject:m_currParsingCoupon];
//        
//    }
//    else if ([elementName isEqualToString:@"motel-rooms"])
//    {
//        m_currSubElementName = @"motel-rooms";
//        
//        m_currParsingRoom = [MotelRoomData new];
//        if (m_currParsingMotel.Rooms == nil) m_currParsingMotel.Rooms = [NSMutableArray new];
//        [m_currParsingMotel.Rooms addObject:m_currParsingRoom];
//    }
    else if ([elementName isEqualToString:@"review"])
    {
        m_currSubElementName = @"review";
        m_currParsingReview = [ReviewData new];
        if (m_currParsingMotel.Reviews == nil) m_currParsingMotel.Reviews = [NSMutableArray new];
        [m_currParsingMotel.Reviews addObject:m_currParsingReview];
    }
    else {


    }
}

-(void) parser:(NSXMLParser *)parser foundCharacters:(NSString *)string{
    //NSLog(@"%@", [tabString stringByAppendingString:string]);
    if ([m_currSubElementName isEqualToString:@"pictures"])
    {
        if ([m_currElementName isEqualToString:@"id"])
        {
            m_currParsingPicture.PictureId = [string intValue];
        }
        
        else if ([m_currElementName isEqualToString:@"flickr-farm-id"])
        {
            m_currParsingPicture.FlickrFarmId = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"flickr-photo-id"])
        {
            m_currParsingPicture.FlickrPhotoId = string;
        }
        else if ([m_currElementName isEqualToString:@"flickr-secret"])
        {
            m_currParsingPicture.FlickrSecret = string;
        }
        else if ([m_currElementName isEqualToString:@"flickr-server-id"])
        {
            m_currParsingPicture.FlickrServerId = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"picture-type"])
        {
            m_currParsingPicture.PictureType = [string intValue];
        }
    }
//    else if ([m_currSubElementName isEqualToString:@"coupons"])
//    {
//        if ([m_currElementName isEqualToString:@"id"])
//        {
//            m_currParsingCoupon.CouponUid = [string intValue];
//        }
//        else if ([m_currElementName isEqualToString:@"coupon_name"])
//        {
//            m_currParsingCoupon.Name = string;
//        }
//    }
//    else if ([m_currSubElementName isEqualToString:@"motel-rooms"])
//    {
//        if ([m_currElementName isEqualToString:@"id"])
//        {
//            m_currParsingRoom.Uid = [string intValue];
//        }
//        
//        else if ([m_currElementName isEqualToString:@"room-name"])
//        {
//            m_currParsingRoom.RoomName = string;
//        }
//    }
    else if ([m_currSubElementName isEqualToString:@"review"])
    {
        if ([m_currElementName isEqualToString:@"id"])
        {
            m_currParsingReview.ReviewUid = [string intValue];
        }
        
        else if ([m_currElementName isEqualToString:@"bad-count"])
        {
            m_currParsingReview.BadCount = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"like-count"])
        {
            m_currParsingReview.LikeCount = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"content"])
        {
            m_currParsingReview.ReviewContent = string;
        }
        else if ([m_currElementName isEqualToString:@"score"])
        {
            m_currParsingReview.Score = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"created-at"])
        {
            
            //문자열이 위치 저장
            NSMutableString *mstrTokenKey = [ NSMutableString stringWithString:string ];
            NSRange range = [mstrTokenKey rangeOfString:@"T" ];
            [mstrTokenKey replaceCharactersInRange:range withString:@" "];
            range = [mstrTokenKey rangeOfString:@"Z" ];
            [mstrTokenKey deleteCharactersInRange:range ];
            
            NSDate *createdDate = [m_dateFormatter dateFromString:mstrTokenKey];
            m_currParsingReview.RegistedTime = [createdDate timeIntervalSinceReferenceDate];
        }
    }
    else
    {
        if ([m_currElementName isEqualToString:@"address"])
        {
            m_currParsingMotel.Address = string;
        }
        else if ([m_currElementName isEqualToString:@"id"])
        {
            m_currParsingMotel.Uid = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"latitude"])
        {
            m_currParsingLatitude = [string floatValue];
        }
        else if ([m_currElementName isEqualToString:@"longitude"])
        {
            float longitude = [string floatValue];

            CLLocation* currentLocation = [[CLLocation alloc] initWithLatitude:m_currParsingLatitude longitude:longitude];
            m_currParsingMotel.Location = currentLocation;
            
        }
        else if ([m_currElementName isEqualToString:@"name"])
        {
            m_currParsingMotel.Name = string;
        }
        else if ([m_currElementName isEqualToString:@"phonenumber"])
        {
            m_currParsingMotel.PhoneNumber = string;
        }
        else if ([m_currElementName isEqualToString:@"guide-for-pos"])
        {
            m_currParsingMotel.GuideForPos = string;
        }
        else if ([m_currElementName isEqualToString:@"guide-for-bus"])
        {
            m_currParsingMotel.GuideForBus = string;
        }
        else if ([m_currElementName isEqualToString:@"guide-for-car"])
        {
            m_currParsingMotel.GuideForCar = string;
        }
        else if ([m_currElementName isEqualToString:@"guide-for-subway"])
        {
            m_currParsingMotel.GuideForSubway = string;
        }
        else if ([m_currElementName isEqualToString:@"guide-for-etc"])
        {
            m_currParsingMotel.GuideForEtc = string;
        }
        else if ([m_currElementName isEqualToString:@"coupon-number"])
        {
            m_currParsingMotel.CouponNumber = string;
        }
        else if ([m_currElementName isEqualToString:@"coupon-from"])
        {
            m_currParsingMotel.CouponFrom = [m_dateFormatter dateFromString:string];
        }
        else if ([m_currElementName isEqualToString:@"coupon-to"])
        {
            m_currParsingMotel.CouponTo = [m_dateFormatter dateFromString:string];
        }
        else if ([m_currElementName isEqualToString:@"coupon-content"])
        {
            m_currParsingMotel.CouponContent = string;
        }
        else if ([m_currElementName isEqualToString:@"coupon-subcontent"])
        {
            m_currParsingMotel.CouponSubContent = string;
        }
        else if ([m_currElementName isEqualToString:@"rent-price-weekdays"])
        {
            m_currParsingMotel.RentPriceWeekdays = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"rent-price-weekend"])
        {
            m_currParsingMotel.RentPriceWeekend = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"stay-price-weekdays"])
        {
            m_currParsingMotel.StayPriceWeekdays = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"stay-price-weekend"])
        {
            m_currParsingMotel.StayPriceWeekend = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"rent-service-hours-weekdays"])
        {
            m_currParsingMotel.RentServiceHoursWeekdays = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"rent-service-hours-weekend"])
        {
            m_currParsingMotel.RentServiceHoursWeekend = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"rent-close-hour-weekdays"])
        {
            m_currParsingMotel.RentCloseHoursWeekdays = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"rent-close-hour-weekend"])
        {
            m_currParsingMotel.RentCloseHoursWeekend = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"stay-begintime-weekdays"])
        {
            m_currParsingMotel.StayBeginTimeWeekdays = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"stay-endtime-weekdays"])
        {
            m_currParsingMotel.StayEndTimeWeekdays = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"stay-begintime-weekend"])
        {
            m_currParsingMotel.StayBeginTimeWeekend = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"stay-endtime-weekend"])
        {
            m_currParsingMotel.StayEndTimeWeekend = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"room-count"])
        {
            m_currParsingMotel.RoomCount = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"image-tag"])
        {
            m_currParsingMotel.ImageId = string;
        }
        else if ([m_currElementName isEqualToString:@"score"])
        {
            m_currParsingMotel.Score = [string intValue];
        }
        else if ([m_currElementName isEqualToString:@"score-count"])
        {
            m_currParsingMotel.ScoreCount = [string intValue];
        }
    }
    
    m_currElementName = @"";
}

-(void) parserDidEndDocument:(NSXMLParser *)parser{
    //NSLog(@"parserDidEndDocument");
    //tabString = nil;
    
    

    // 사진 데이터 로딩 처리
    //   - 우선 정상적으로 파싱된 데이터 추출
    /*
    NSMutableIndexSet *removeSet = [NSMutableIndexSet new];
    int index = 0;
    for (PictureData *p in m_loadingPictures)
    {
        NSMutableDictionary *pic_dic = [[CachingMotelData sharedInstance].CachingPictureList objectForKey:p.ImageId];
        if (pic_dic == nil) pic_dic = [NSMutableDictionary new];
        if (p.PictureId == -1)
        {
            [removeSet addIndex:index];
        }
        else if ([pic_dic objectForKey:p.])
        {
            [removeSet addIndex:index];
        }
        else
        {
            [pic_dic setObject:p forKey:p.FlickrPhotoId];
            [p loadFullsizeWithFlickr];
        }
        
        ++index;
    }
    [m_loadingPictures removeObjectsAtIndexes:removeSet];
    */
    //
    
    
    for (MotelInfo *motel in m_motelsResult)
    {

        time_t from = [motel.CouponFrom timeIntervalSinceReferenceDate];
        time_t to = [motel.CouponTo timeIntervalSinceReferenceDate];
        time_t curr = [NSDate timeIntervalSinceReferenceDate];
        
        if (curr > from && curr < to) motel.CouponEnable = TRUE;
        else motel.CouponEnable = FALSE;
        

        
        NSCalendar *gregorian = [NSCalendar currentCalendar];
        NSDateComponents *weekDayComponents = [gregorian components:NSWeekdayCalendarUnit fromDate:[NSDate date]];
        NSInteger mDay = [weekDayComponents weekday];
        if (mDay == 0 || mDay == 6) motel.Price = motel.StayPriceWeekend;
        else motel.Price = motel.StayPriceWeekdays;
      
        [[CachingMotelData sharedInstance] UpdateMotelInfo:motel];
        MotelInfo *caching_motel = [[CachingMotelData sharedInstance] GetMotelInfoById:motel.Uid];
        CachingMotelData *cachingData = [CachingMotelData sharedInstance];
        [cachingData.FoundMotelList setObject:caching_motel forKey:[NSString stringWithFormat:@"%d", motel.Uid]];
        
        NSLog(@"%@ => (%f, %f)",caching_motel.Name, caching_motel.Location.coordinate.latitude, caching_motel.Location.coordinate.longitude);
        
        
        if (motel.ScoreCount > 0) motel.ScoreAve = motel.Score/motel.ScoreCount;
        else motel.ScoreAve = 0;
        
        if (motel.ImageId) [self requestPhotos:motel.ImageId wIndicator:nil];
    }
    
    
    if (m_indicatorBackgroundView)
    {
        [self stopIndiator];
    }
    [_delegate responseFromServer:m_motelsResult];
}

- (void)startIndiator:(UIView *)indicatorView
{
    if (indicatorView)
    {
        if (m_indicatorBackgroundView == nil)
        {
            m_indicatorBackgroundView = [[UIView alloc] initWithFrame:indicatorView.frame];
            //m_indicatorBackgroundView = [[UIView alloc] initWithFrame:CGRectMake(<#CGFloat x#>, <#CGFloat y#>, <#CGFloat width#>, <#CGFloat height#>)];
            //m_indicatorBackgroundView.backgroundColor = [UIColor blackColor];
            UIColor *black = [UIColor colorWithRed:0.0f green:0.0f blue:0.0f alpha:0.8f];
            [m_indicatorBackgroundView setBackgroundColor:black];
            //m_indicatorBackgroundView.alpha = 0.5;
            
            UIButton *cancelBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
            
            [cancelBtn setFrame:CGRectMake(110, 190, 100, 30)];
            cancelBtn.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
            cancelBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
            [cancelBtn setTitle:@"취소" forState: UIControlStateNormal];
            /*
             [cancelBtn setTitleColor:[UIColor whiteColor] forState: UIControlStateNormal];
             cancelBtn.backgroundColor = [UIColor colorWithRed:110.f/255.f green:108.f/255.f blue:106.f/255.f alpha:1];
             */
            [cancelBtn addTarget:self action:@selector(clickUpdateCancelButton:) forControlEvents:UIControlEventTouchUpInside];
            [m_indicatorBackgroundView addSubview:cancelBtn];
            m_indicatorBackgroundView.tag = 10000;
        }
        else
        {
            //m_indicatorBackgroundView.frame = indicatorView.frame;
            m_indicatorBackgroundView.hidden = FALSE;
        }
        
        
        
        /*
         UIAlertView *alert = [[UIAlertView alloc]initWithTitle:@"A" message:@"" delegate:self cancelButtonTitle:@"확인" otherButtonTitles:@"취소", nil];
         [alert show];
         [alert setHidden:TRUE];
         */
        
        if (m_indicator == nil)
        {
            m_indicator = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(135, 140, 50, 50)];
            m_indicator.hidesWhenStopped = YES;
            [m_indicatorBackgroundView addSubview:m_indicator];
        }
        
        if ([indicatorView viewWithTag:m_indicatorBackgroundView.tag] == nil)
        {
            [indicatorView addSubview:m_indicatorBackgroundView];
        }
        [m_indicator startAnimating]; // 애니메이션 시작
    }
}

- (void)clickUpdateCancelButton:(id)sender
{
    [self stopIndiator];
}

- (void)stopIndiator
{
    m_indicatorBackgroundView.hidden = TRUE;
    [m_indicator stopAnimating]; // 애니메이션 중지
}


- (void) requestPhotos:(NSString *)tags wIndicator:(UIView *)indicatorView
{
    NSString *user_id=@"91720682@N05";
    NSString *api_key=@"da300a7c8ae6a95b0311334db5dc7991";
    
    NSString *url = [NSString stringWithFormat:@"http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=%@&user_id=%@&text=%@&format=json",api_key, user_id, tags];
    NSLog(@"%@",url);
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedPhotos:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void)receivedPhotos:(id)sender
{
    [self stopIndiator];
    if (sender == nil)
    {
        return;
    }
    
    
    NSString *json_string = sender;
    
    NSRange range = [json_string rangeOfString:@"{"];
    NSRange range_reverse = [json_string rangeOfString:@"}" options:NSBackwardsSearch];
    
    if (range.location != NSNotFound
        && range_reverse.location != NSNotFound) {
        
        range.length = range_reverse.location - range.location+1;
        NSString *result = [json_string substringWithRange:range];
        
        NSDictionary *obj_list = [_parser objectWithString:result];
        if (obj_list == nil)
        {
            NSLog(@"%@",_parser.error);
        }
        //for (NSDictionary *object in obj_list)
        {
            NSDictionary *photos_info = [obj_list valueForKey:@"photos"];
            NSArray *photos = [photos_info valueForKey:@"photo"];
            for (NSDictionary *photo in photos)
            {
                NSString *photo_title = [photo valueForKey:@"title"];
                NSString *photo_id = [photo valueForKey:@"id"];
                NSString *photo_secret = [photo valueForKey:@"secret"];
                NSString *photo_server = [photo valueForKey:@"server"];
                NSString *photo_farm = [photo valueForKey:@"farm"];
                
                PictureData *photo_data = [PictureData new];
                photo_data.FlickrFarmId = [photo_farm intValue];
                photo_data.FlickrServerId = [photo_server intValue];
                photo_data.FlickrPhotoId = photo_id;
                photo_data.FlickrSecret = photo_secret;
                photo_data.ImageTitle = photo_title;
                
                
                NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:@"\\[i_[0-9]+\\]" options:NSRegularExpressionCaseInsensitive error:nil];
                NSRange rangeOfFirstMatch = [regex rangeOfFirstMatchInString:photo_title options:0 range:NSMakeRange(0, [photo_title length])];
                if (rangeOfFirstMatch.location == NSNotFound) continue;
                NSString *strImageIdTitle = [photo_title substringWithRange:rangeOfFirstMatch];
                
                NSRegularExpression *regex_number = [NSRegularExpression regularExpressionWithPattern:@"[0-9]+" options:NSRegularExpressionCaseInsensitive error:nil];
                NSRange rangeOfNumber = [regex_number rangeOfFirstMatchInString:strImageIdTitle options:0 range:NSMakeRange(0, [strImageIdTitle length])];
                if (rangeOfNumber.location == NSNotFound) continue;
                NSString *strImageId = [strImageIdTitle substringWithRange:rangeOfNumber];
                
                if (strImageId)
                {
                    NSMutableDictionary *pic_dic = [[CachingMotelData sharedInstance].CachingPictureList objectForKey:strImageId];
                    if (pic_dic == nil) pic_dic = [NSMutableDictionary new];
                    
                    PictureData *caching_photo = [pic_dic objectForKey:photo_title];
                    if (!caching_photo)
                    {
                        [pic_dic setObject:photo_data forKey:photo_title];
                        
                        
                        NSString *motelId = [[CachingMotelData sharedInstance].CachingImageIdToMotelIdMap objectForKey:strImageId];
                        MotelInfo *motel = [[CachingMotelData sharedInstance].CachingMotelList objectForKey:motelId];
                        if (motel.Pictures == nil) motel.Pictures = [NSMutableArray new];
                        [motel.Pictures addObject:photo_data];

                        [photo_data setPictureName:photo_title];
                        [photo_data setUseCaching:TRUE];

                        [photo_data loadFullsizeWithFlickr];
                    }
                }
                
            }

        }
        
        [CachingMotelData sharedInstance].LastPictureUpdateTime = [NSDate timeIntervalSinceReferenceDate];

    }
    
}

- (void) requestLoadCommunity:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView type:(int)msgType
{
    NSString *url = nil;
    if (msgType == 0) {
        url = [NSString stringWithFormat:@"%@/get_msg_talk",GAE_SVR_ADDR];
    }
    else if (msgType == 1) {
        url = [NSString stringWithFormat:@"%@/get_msg_bbs",GAE_SVR_ADDR];
    }
    else if (msgType == 2) {
        url = [NSString stringWithFormat:@"%@/get_msg_photo",GAE_SVR_ADDR];
    }
    else if (msgType == 3) {
        url = [NSString stringWithFormat:@"%@/get_timeline",GAE_SVR_ADDR];
    }
    
//[NSString stringWithFormat:@"%@/load_community",GAE_SVR_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedCommunityMsg:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void)receivedCommunityMsg:(id)sender
{
    [self stopIndiator];
    [_delegate responseCommunityLoad:sender];
}


- (void) requestLoadTalkMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/get_msg_talk",GAE_SVR_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedTalkMsg:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}
- (void)receivedTalkMsg:(id)sender
{
    [self stopIndiator];
//    [_delegate responseTalkMsg:sender];
}


- (void) requestLoadPhotoMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/get_msg_photo",GAE_SVR_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedPhotoMsg:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}
- (void)receivedPhotoMsg:(id)sender
{
    [self stopIndiator];
//    [_delegate responsePhotoMsg:sender];
    [_delegate responseCommunityLoad:sender];
}


- (void) requestLoadBBSMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/get_msg_bbs",GAE_SVR_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedBBSMsg:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}
- (void)receivedBBSMsg:(id)sender
{
    [self stopIndiator];
    [_delegate responseBBSMsg:sender];
}



- (void) requestLoadDirectMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/get_dm",GAE_SVR_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedDirectMsg:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}
- (void)receivedDirectMsg:(id)sender
{
    [self stopIndiator];
    [_delegate responseDirectMsg:sender];
}

- (void) requestUpdateDirectMsg:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/update_dm",GAE_SVR_ADDR];
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;//[NSDictionary dictionaryWithObjectsAndKeys:@"latitude",@"name",@"longitute", @"home", nil];
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedUpdateDirectMsg:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    if (indicatorView) [self startIndiator:indicatorView];
}

- (void)receivedUpdateDirectMsg:(id)sender
{
    [self stopIndiator];
    [_delegate responseDirectMsg:sender];
}


- (void) requestCommunityMsg:(NSDictionary *)conditions Type:(int)msgtype attachPhoto:(UIImage*)image photoName:(NSString *)photo_name wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/update_msg_talk",GAE_SVR_ADDR];
    
    if (msgtype == 1)
    {
        url = [NSString stringWithFormat:@"%@/update_msg_bbs",GAE_SVR_ADDR];
    }
    else if (msgtype == 2)
    {
        url = [NSString stringWithFormat:@"%@/update_msg_photo",GAE_SVR_ADDR];
    }
    else if (msgtype == 3)
    {
        url = [NSString stringWithFormat:@"%@/update_timeline",GAE_SVR_ADDR];
    }
    
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedUpdateMsg:)];
    
    // 페이지 호출
    [httpRequest requestUrl:url bodyObject:bodyObject attachPhoto:image photoName:photo_name];
   
    if (indicatorView) [self startIndiator:indicatorView];
}
- (void)receivedUpdateMsg:(id)sender
{
    [self stopIndiator];
    
    [_delegate responseCommunityLoad:sender];
}

- (void) requestUpdateProfile:(NSDictionary *)conditions attachPhoto:(UIImage *)image photoName:(NSString *)photo_name wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/update_profile",GAE_SVR_ADDR];
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedProfileInfo:)];
    
    [httpRequest requestUrl:url bodyObject:bodyObject attachPhoto:image photoName:photo_name];

    if (indicatorView) [self startIndiator:indicatorView];
}
- (void) requestLoadProfile:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/load_profile",GAE_SVR_ADDR];
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedProfileInfo:)];

    [httpRequest requestUrl:url bodyObject:bodyObject];
 
    
    if (indicatorView) [self startIndiator:indicatorView];
}
- (void) requestAuthAdult:(NSDictionary *)conditions wIndicatorView:(UIView *)indicatorView
{
    NSString *url = [NSString stringWithFormat:@"%@/auth_adult",GAE_SVR_ADDR];
    // HTTP Request 인스턴스 생성
    HTTPRequest *httpRequest = [[HTTPRequest alloc] init];
    
    // POST로 전송할 데이터 설정
    NSDictionary *bodyObject = conditions;
    
    // 통신 완료 후 호출할 델리게이트 셀렉터 설정
    [httpRequest setDelegate:self selector:@selector(receivedProfileInfo:)];
    
    [httpRequest requestUrl:url bodyObject:bodyObject];
    
    
    if (indicatorView) [self startIndiator:indicatorView];
    
}

- (void)receivedProfileInfo:(id)sender
{
    [self stopIndiator];
    
    [_delegate responseProfile:sender];
}

@end
