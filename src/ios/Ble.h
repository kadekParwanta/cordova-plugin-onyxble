//
//  Ble.h
//  onyxB
//
//  Created by Nomi on 21/11/2015.
//
//

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>

#import <OnyxBeaconLib/OnyxBeacon.h>

@interface Ble : CDVPlugin<OnyxBeaconContentDelegate, OnyxBeaconDelegate>{
    NSDictionary *notificationMessage;
    BOOL    isInline;
}

@property (nonatomic, readwrite, strong) NSMutableDictionary* settings;
@property (nonatomic, readwrite, strong) NSXMLParser* configParser;
@property (nonatomic, readwrite, strong) NSString* configFile;

- (void) addWebListener:(CDVInvokedUrlCommand *)command;
- (void) addOnyxBeaconsListener: (CDVInvokedUrlCommand*)command;
- (void) addTagsListener: (CDVInvokedUrlCommand*)command;
- (void) addCouponsListener:(CDVInvokedUrlCommand *) command;
- (void) addDeliveredCouponsListener:(CDVInvokedUrlCommand *) command;
- (void) getDeliveredCoupons:(CDVInvokedUrlCommand *) command;
- (void) setErrorListener:(CDVInvokedUrlCommand *)command;
- (void) initSDK: (CDVInvokedUrlCommand*)command;
- (void) stop:(CDVInvokedUrlCommand *)command;
- (void) enterBackground:(CDVInvokedUrlCommand *)command;
- (void) enterForeground:(CDVInvokedUrlCommand *)command;
- (void) showCoupon:(CDVInvokedUrlCommand *)command;
- (void) getTags:(CDVInvokedUrlCommand *)command;
- (void) buzzBeacon:(CDVInvokedUrlCommand *)command;
- (void) addPushListener:(CDVInvokedUrlCommand *)command;

//Push Notifications
@property (nonatomic, strong) NSDictionary *notificationMessage;
@property BOOL                          isInline;

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)setNotificationMessage:(NSDictionary *)notification;
- (void)notificationReceived;

@end