//
//  OBBeaconRegion.h
//  OnyxBeacon
//
//  Created by Igor Stirbu on 12/12/13.
//  Copyright (c) 2013 RomVentures SRL. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@interface OBBeaconRegion : NSObject<NSCoding>

@property (nonatomic, strong, readonly) NSUUID *UUID;
@property (nonatomic, strong, readonly) NSNumber *major;
@property (nonatomic, strong, readonly) NSNumber *minor;
@property (nonatomic, strong, readonly) NSString *name;
@property (nonatomic, strong, readonly) NSString *desc;
@property (nonatomic, assign, readonly) BOOL enabled;

@end
