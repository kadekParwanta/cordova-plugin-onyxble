//
//  OBBeacon.h
//  OnyxBeacon
//
//  Created by Igor Stirbu on 12/12/13.
//  Copyright (c) 2013 RomVentures SRL. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

typedef NS_ENUM(NSInteger, OBBroadcastingScheme) {
    OBBroadcastingSchemeUnknown = -1,
    OBBroadcastingSchemeiBeacon = 0,
    OBBroadcastingSchemeEddystoneUID,
    OBBroadcastingSchemeEddystoneURL,
    OBBroadcastingSchemeEddystone
};

@class OBDevice;
@class OBEddystoneTelemetry;

#define OBBeaconUpdatedProximity @"OBBeaconUpdatedProximity"
#define OBBeaconUpdatedProximityBeaconKey @"beacon"

@interface OBBeacon : NSObject<NSCoding>

@property (nonatomic, strong, readonly) NSUUID * _Nullable uuid;
@property (nonatomic, strong, readonly) NSNumber * _Nullable major;
@property (nonatomic, strong, readonly) NSNumber * _Nullable minor;

@property (nonatomic, assign, readonly) OBBroadcastingScheme broadcastingScheme;
@property (nonatomic, assign, readonly) CLProximity proximity;
@property (nonatomic, assign, readonly) CLProximity lastProximity;
@property (nonatomic, assign, readonly) BOOL proximityChanged;
@property (nonatomic, assign, readonly) NSInteger rssi;
@property (nonatomic, readonly) CLLocationAccuracy accuracy;

@property (nonatomic, strong, readonly) NSDate * _Nullable rangedTime;
@property (nonatomic, strong, readonly) NSDate * _Nullable lastSeen;
@property (nonatomic, strong, readonly) NSString * _Nullable umm;

@property (nonatomic, strong, readonly) OBDevice * _Nullable device;

@property (nonatomic, strong, readonly) NSSet * _Nullable tags;

- (BOOL)isAccountBeacon;

@end
