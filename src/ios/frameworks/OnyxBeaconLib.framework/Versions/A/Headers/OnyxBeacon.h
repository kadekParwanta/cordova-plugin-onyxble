//
//  OnyxBeacon.h
//  OnyxBeacon
//
//  Created by Igor Stirbu on 11/02/14.
//  Copyright (c) 2014 RomVentures SRL. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <CoreBluetooth/CoreBluetooth.h>

#import "OBContent.h"
#import "OBBeacon.h"
#import "OBBeaconRegion.h"
#import "OBTag.h"

@class OnyxBeacon;

@protocol OnyxBeaconDelegate <NSObject>

@optional

/**
 * Sends an error notification to the delegate regarding the functionality of the system libraries.
 *
 * @param error Error message regarding the functionality of the system libraries.
 */
- (void)onyxBeaconError:(NSError *)error;

/**
 * Tells the delegate that one or more beacons are in range.
 *
 * @param beacons An array of OBBeacon objects representing the beacons currently in range. If beacons is empty, you can assume that no beacons matching the specified region are in range. When a specific beacon is no longer in beacons, that beacon is no longer received by the device. You can use the information in the OBBeacon objects to determine the range of each beacon and its identifying information.
 * @param region The region object containing the parameters that were used to locate the beacons.
 */
- (void)didRangeBeacons:(NSArray *)beacons inRegion:(OBBeaconRegion *)region;

/**
 * Tells the delegate that the user entered the specified region.
 *
 * @param region The region that was entered.
 */
- (void)locationManagerDidEnterRegion:(CLRegion *)region;

/**
 * Tells the delegate that the user left the specified region.
 *
 * @param region The region that was exited.
 */
- (void)locationManagerDidExitRegion:(CLRegion *)region;

/**
 * Invoked when the central manager’s state is updated.
 *
 *  @param state The current state of the central.
 *
 *  @constant CBCentralManagerStateUnknown = 0   State unknown, update imminent.
 *  @constant CBCentralManagerStateResetting     The connection with the system service was momentarily lost, update imminent.
 *  @constant CBCentralManagerStateUnsupported   The platform doesn't support the Bluetooth Low Energy Central/Client role.
 *  @constant CBCentralManagerStateUnauthorized  The application is not authorized to use the Bluetooth Low Energy Central/Client role.
 *  @constant CBCentralManagerStatePoweredOff    Bluetooth is currently powered off.
 *  @constant CBCentralManagerStatePoweredOn     Bluetooth is currently powered on and available to use.
 *
 */
- (void)bluetoothCentralManagerDidUpdateState:(NSInteger)state;

/**
 * Invoked when the central manager start broadcasting as an iBeacon
 * If advertisement could not be started, the cause will be detailed in the <i>error</i> parameter.
 *
 */
- (void)peripheralManagerDidStartAdvertisingWithError:(NSError *)error;

@end

@protocol OnyxBeaconContentDelegate <NSObject>

/**
 * Tells the delegate that one or more coupons are received.
 *
 * @param coupons An array of OBContent objects representing the coupons that need to be shown to the user.
 */
- (void)didReceiveContent:(NSArray *)coupons;

/**
 * The default views have the option to display addition information and if the users wants to access it, this delegate method will be called.
 *
 * @param content The coupon that is displayed.
 * @param viewController The view controller that displayed the coupon.
 */
- (void)didRequestInfo:(OBContent *)content inViewController:(UIViewController *)viewController;

@end

extern const NSString *OnyxBeaconErrorDomain;

typedef NS_ENUM(NSInteger, OnyxBeaconErrorCodes) {
    OnyxBeaconLocationServicesDisabled = 1,
    OnyxBeaconLocationServicesRestricted,
    OnyxBeaconErrorReadingBeaconInfo,
    OnyxBeaconTimedOutScanningBeacons,
    OnyxBeaconBackgroundRefreshDisabled,
    OnyxBeaconBackgroundRefreshRestricted,
    OnyxBeaconBluetoothDisabled,
    OnyxBeaconBLEError,
    OnyxBeaconConfigRejected,
    OnyxBeaconConfigInvalidPacket,
    OnyxBeaconAuthError,
    OnyxBeaconFirmwareUpgradeInvalidImage,
    OnyxBeaconFirmwareUpgradeDeviceReset,
    OnyxBeaconFirmwareUpgradeCouldNotConnect,
    OnyxBeaconFirmwareUpgradeInvalidResponse,
    OnyxBeaconFirmwareUpgradeInvalidBlock,
    OnyxBeaconFirmwareUpgradeImageNotFound,
    OnyxBeaconPeripheralConnTimedOutError,
    OnyxBeaconOperationCancelledError,
};

typedef NS_ENUM(NSInteger, OBContentAction) {
    OBContentActionOpened,
    OBContentActionTapped,
    OBContentActionRemoved
};

typedef void (^OnyxBeaconLogger)(NSString *message);
typedef void (^OnyxBeaconSendLogHandler)(NSError*);
typedef void (^OnyxBeaconPushNotificationsHandler)(NSDictionary*,  NSError*);

@interface OnyxBeacon : NSObject

/**
 * The delegate object to receive beacon events.
 *
 * @see OnyxBeaconDelegate
 */
@property (nonatomic, strong) id<OnyxBeaconDelegate> delegate;

/**
 * The delegate object to receive content
 * The SDK will take care of detecting beacons, managing beacons and proximity, requesting for corresponding content. It will also make necessary metric calls to backend to track user activity.
 *
 * @see OnyxBeaconContentDelegate
 */
@property (nonatomic, strong) id<OnyxBeaconContentDelegate> contentDelegate;

/**
 *
 * System alert about bluetooth status. Default is YES.
 */
@property (nonatomic, assign) BOOL showBluetoothPowerAlertKey;

/**
 * OnyxBeacon SDK version
 */
@property (nonatomic, strong, readonly) NSString *version;

/**
 * OnyxBeacon service instance is created and accessed through the sharedInstance: class method
 *
 */
+ (instancetype)sharedInstance;

// Call in application:didFinishLaunchingWithOptions:

/**
 * Sets clientID and secret, enabling communication with the OnyxBeacon Cloud API.
 *
 * The values for client id and client secret should be obtained from the Apps section in CMS.
 *
 * @param clientID Client identifier.
 * @param secret Client secret.
 */
- (void)startServiceWithClientID:(NSString *)clientID secret:(NSString *)secret;

/**
 * Sets external device identifier
 *
 * @param installIdentifier Your custom device identifier.
 */
- (void)setYourInstallIdentifier:(NSString*)installIdentifier;

/**
 * Reset all data from the SDK. (Cleanup)
 */
- (void)resetService;

/**
 * Sets logger for debug
 */
- (void)setLogger:(OnyxBeaconLogger)logger;

/**
 * @deprecated This method is deprecated starting in version x.x
 * @note Please use @code appWillResignActive @endcode instead.
 * Call in applicationDidEnterBackground: method.
 */
- (void)didEnterBackground __attribute__((deprecated));

/**
 * Call in applicationWillResignActive: method.
 */
- (void)appWillResignActive;

/**
 * Call in applicationWillEnterForeground: method.
 */
- (void)willEnterForeground;

/**
 * For iOS8 message for authorization should be added to Info.plist.
 * Right-click on Info.plist and select Show Raw Key/Values. Add a new entry with key 'NSLocationAlwaysUsageDescription' and value description of the reason for the 'Always' authorization.
 *
 * For 'WhenInUse' authorization add the key 'NSLocationWhenInUseUsageDescription' with the reason for this authorization. If the corresponding description will not be added then the OS will not present the authorization request.
 *
 */
- (void)requestAlwaysAuthorization;
- (void)requestWhenInUseAuthorization;

// Methods for Coupon Delivery
/**
 * Get all the coupons that were received until now.
 *
 */
- (NSArray *)getContent;

/**
 * Delete a coupon in order to receive it again next time.
 *
 * @param content The coupon that need to be deleted.
 */
- (void)deleteContent:(OBContent *)content;

/**
 * SDK provides default method for displaying content.
 *
 * @param content The coupon that will be displayed.
 */
- (UIViewController *)viewControllerForContent:(OBContent *)content;

/**
 * @param content The coupon that will be displayed.
 * @param viewController
 */
- (void)showContentInfo:(OBContent *)content inViewController:(UIViewController *)viewController;

/**
 * Clear all the received coupons.
 *
 */
- (void)clearCoupons;

/**
 * Image and web content are displayed in a view controller that contains an image view or a web browser for rendering. Opened events for this content are tracked automatically by SDK/CMS though image and action URLs.
 * If the content does not have such representation then use this method to set the 'Opened' event.
 *
 * @param content The coupon that was opened
 */
- (void)contentOpened:(OBContent *)content;

/**
 * Image and web content are displayed in a view controller that contains an image view or a web browser for rendering. Tapped events for this content are tracked automatically by SDK/CMS though image and action URLs.
 * If the content does not have such representation then use this method to set the 'Tapped' event.
 *
 * @param content The coupon that was tapped
 */
- (void)contentTapped:(OBContent *)content;

/**
 * OnyxBeacon SDK supports collection of user metrics.
 * For example, using Facebook integration it is possible to send metrics after Facebook login.
 *
 * @param userMetrics example: {
                     "userMetrics":{
                     "type" : "FB", // required
                     "id":1310972775, // required
                     "birthday":"12\/03\/1982", // required for social campaigns
                     "gender":"male", // required for social campaigns
                     "first_name":"Tim",
                     "last_name":"Smith",
                     "link":"https:\/\/www.facebook.com\/tim.smith",
                     "locale":"en_US",
                     "name":"Tim Smith",
                     "updated_time":"2013-10-12T13:24:07+0000",
                     "username":"tim.smith",
                     "verified":1
                     }
                    }
 */
- (void)sendUserMetrics:(NSDictionary *)userMetrics;

/**
 * Get all the tags defined in the CMS
 *
 */
- (NSArray *)getTags;

/**
 * Get selected tags
 *
 */
- (NSSet *)getSelectedTags;

/**
 * Set tags as selected
 *
 * @param tags The selected tags.
 */
- (void)setTags:(NSSet *)tags;

/**
 * SDK provides default method for displaying tags. You cand select tags using this view.
 *
 */
- (UIViewController *)viewControllerForTags;

/**
 * Send logs from the app. Used for developement.
 *
 * @param data The logs data.
 * @param reporter User identifier.
 * @param message Extra message.
 */
- (void)sendReport:(NSData *)data reporter:(NSString *)reporter message:(NSString *)message handler:(OnyxBeaconSendLogHandler)handler;

/**
 * Register for push notification
 *
 * @param deviceToken - Apple device token using "application:didRegisterForRemoteNotificationsWithDeviceToken:" method
 * @param pushProvider = @"IBMBluemix"
 */
- (void)registerForPushNotificationWithDeviceToken:(NSData *)deviceToken
                                       forProvider:(NSString*)pushProvider
                                           handler:(OnyxBeaconPushNotificationsHandler)handler;

/**
 * Sets provider device token
 *
 * @param providerDeviceToken = deviceToken from push provider
 */
- (void)sendPushNotificationProviderDeviceToken:(NSString *)providerDeviceToken;

/**
 * Start broadcasting the iOS device as an iBeacon.
 *
 * @param proximityUUID The `proximityUUID` to advertise.
 * @param major The `major` to advertise.
 * @param minor The `minor` to advertise.
 * @param identifier The identifier of the region used to advertise.
 */
- (void)startBroadcastingWithProximityUUID:(NSUUID *)proximityUUID
                                    major:(CLBeaconMajorValue)major
                                    minor:(CLBeaconMinorValue)minor
                               identifier:(NSString *)identifier;

/**
 * Stop broadcasting as an iBeacon.
 *
 */
- (void)stopBroadcasting;


/**
 * Start buzzing an OBBeacon.
 *
 * @param beacon The `beacon` that you want to buzz.
 *
 */
- (NSError *)buzzBeacon:(OBBeacon *)beacon;

@end
