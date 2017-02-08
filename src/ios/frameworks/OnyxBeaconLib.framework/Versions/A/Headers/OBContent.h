//
//  OBContent.h
//  OnyxBeacon
//
//  Created by Igor Stirbu on 12/12/13.
//  Copyright (c) 2013 RomVentures SRL. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, ContentType) {
    ContentTypeText = 3,
    ContentTypeWeb = 2,
    ContentTypeImage = 1,
};

typedef NS_ENUM(NSInteger, ContentState) {
    ContentStateInit,
    ContentStateSent,
    ContentStateUnread,
    ContentStateRead,
    ContentStateSaved,
    ContentStateArchived
};

@interface OBContent : NSObject<NSCoding>

@property (nonatomic, strong, readonly) NSString *title;
@property (nonatomic, strong, readonly) NSNumber *uuid;
@property (nonatomic, strong, readonly) NSString *message;
@property (nonatomic, strong, readonly) NSString *couponDescription;

@property (nonatomic, strong, readonly) NSString *path;
@property (nonatomic, strong, readonly) NSString *action;
@property (nonatomic, strong, readonly) NSDate *createTime;
@property (nonatomic, strong, readonly) NSDate *expirationDate;

@property (nonatomic, assign) ContentState contentState;
@property (nonatomic, assign) ContentType contentType;

- (BOOL)contentExpired;

@end
