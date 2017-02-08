//
//  OBTag.h
//  OnyxBeaconLib
//
//  Created by Macbook on 05/12/14.
//  Copyright (c) 2014 RomVentures SRL. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface OBTag : NSObject<NSCoding>

@property (nonatomic, strong, readonly) NSNumber *tagId;
@property (nonatomic, strong, readonly) NSString *name;

@end
