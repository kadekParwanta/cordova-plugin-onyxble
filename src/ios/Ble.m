//
//  Ble.m
//  onyxB
//
//  Created by Nomi on 21/11/2015.
//
//

#import "Ble.h"
#import "AFNetworkActivityLogger.h"
#import <Cordova/CDVConfigParser.h>
#import <Cordova/NSDictionary+CordovaPreferences.h>

@interface Ble ()

@property (nonatomic, copy) void (^rangeBeaconsHandler)(NSArray *beacons, OBBeaconRegion *region);
@property (nonatomic, copy) void (^errorHandler)(NSError *error);
@property (nonatomic, copy) void (^couponHandler)(NSArray *coupons);

@end


@implementation Ble

NSMutableArray *rangeBeaconsListeners;
NSMutableArray *couponsListeners;
NSString *errorCallbackId;
NSMutableArray *deliveredCouponsListeners;
NSDictionary *preferences;

/*
 NSDictionary *jsonObj = [ [NSDictionary alloc]
 initWithObjectsAndKeys :
 dateStr, @"dateStr",
 @"true", @"success",
 nil
 ];
 */

- (void)pluginInitialize {
    _rangeBeaconsHandler = [self createRangeBeaconsHandler];
    _errorHandler = [self createErrorHandler];
    _couponHandler = [self createCouponHandler];
    CDVConfigParser *delegate = [[CDVConfigParser alloc] init];
    [self parseSettingsWithParser:delegate];
    self.settings = delegate.settings;
}

-(void)parseSettingsWithParser:(NSObject<NSXMLParserDelegate>*) delegate
{
    NSString* path = [self configFilePath];
    NSURL* url = [NSURL fileURLWithPath:path];
    self.configParser = [[NSXMLParser alloc] initWithContentsOfURL:url];
    if (self.configParser == nil) {
        NSLog(@"Failed to initialize XML parser.");
        return;
    }
    
    [self.configParser setDelegate:((id <NSXMLParserDelegate>) delegate)];
    [self.configParser parse];
    
}

-(NSString*) configFilePath {
    NSString* path = self.configFile ?: @"config.xml";
    if (![path isAbsolutePath]) {
        NSString* absolutePath = [[NSBundle mainBundle] pathForResource:path ofType:nil];
        if (!absolutePath) {
            NSAssert(NO, @"ERROR: %@ not found in the main bundle!", path);
        }
        path = absolutePath;
    }
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        NSAssert(NO, @"ERROR: %@ does not exist. Please run cordova-ios/bin/cordova_plist_to_config_xml path/to/project.", path);
        return nil;
    }
    
    return path;
}

#pragma mark - Coupan View

- (void)addCouponsListener:(CDVInvokedUrlCommand *) command{
    if (couponsListeners == nil) {
        couponsListeners = [[NSMutableArray alloc] init];
    }
    [couponsListeners addObject:command.callbackId];
}

- (void)addDeliveredCouponsListener:(CDVInvokedUrlCommand *) command{
    if (deliveredCouponsListeners == nil) {
        deliveredCouponsListeners = [[NSMutableArray alloc] init];
    }
    [deliveredCouponsListeners addObject:command.callbackId];
}

- (void)getDeliveredCoupons:(CDVInvokedUrlCommand *) command{
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString:@"getDeliveredCoupon is invoked"];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
    // in android, the delivered coupons is retrieved via broadcast receiver, while in iOS synchronously
    
    NSArray *coupons = [[OnyxBeacon sharedInstance] getContent];
    
    CDVPluginResult* contentResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                     messageAsArray: coupons];
    
    [contentResult setKeepCallbackAsBool:YES];
    for (NSString *callbackId in deliveredCouponsListeners) {
        [self.commandDelegate sendPluginResult:contentResult callbackId:callbackId];
    }
}


-(void)addOnyxBeaconsListener:(CDVInvokedUrlCommand *)command {
    if (rangeBeaconsListeners == nil) {
        rangeBeaconsListeners = [[NSMutableArray alloc] init];
    }
    [rangeBeaconsListeners addObject:command.callbackId];
}

-(void) addWebListener:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        [[OnyxBeacon sharedInstance] setLogger:^(NSString *message) {
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
            [result setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
        [[AFNetworkActivityLogger sharedLogger] setLoggerBlock:^(NSString *message) {
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
            [result setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
        [[AFNetworkActivityLogger sharedLogger] setLevel:AFLoggerLevelDebug];
        [[AFNetworkActivityLogger sharedLogger] startLogging];
    }];
}

-(void)setErrorListener:(CDVInvokedUrlCommand *)command {
    errorCallbackId = command.callbackId;
}

-(void (^)(NSArray *beacons, OBBeaconRegion *region)) createRangeBeaconsHandler {
    return ^(NSArray *beacons, OBBeaconRegion *region) {
        NSMutableArray * results = [[NSMutableArray alloc] init];
        for ( int i = 0, size = (int) beacons.count; i< size; i++) {
            NSMutableDictionary* beacon = [beacons objectAtIndex:i];
            NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"dd-MM-yyy HH:mm:ss ZZZ"];
            
            [dict setValue:[NSString stringWithFormat:@"%@", region.UUID] forKey:@"proximityUuid"];
            [dict setValue:[NSString stringWithFormat:@"%@", [beacon valueForKey:@"uuid"]] forKey:@"uuid"];
            [dict setValue:[beacon valueForKey:@"major"] forKey:@"major"];
            [dict setValue:[beacon valueForKey:@"minor"] forKey:@"minor"];
            [dict setValue:[beacon valueForKey:@"broadcastingScheme"] forKey:@"broadcastingScheme"];
            [dict setValue:[beacon valueForKey:@"lastProximity"] forKey:@"lastProximity"];
            
            NSDate *rangedTime = [beacon valueForKey:@"rangedTime"];
            [dict setValue:[dateFormatter stringFromDate:rangedTime] forKey:@"rangedTime"];
            
            NSDate *lastSeen = [beacon valueForKey:@"lastSeen"];
            [dict setValue:[dateFormatter stringFromDate:lastSeen] forKey:@"lastSeen"];
            [dict setValue:[beacon valueForKey:@"umm"] forKey:@"umm"];
            [dict setValue:[beacon valueForKey:@"rssi"] forKey:@"rssi"];
            [dict setValue:[beacon valueForKey:@"proximity"] forKey:@"proximity"];
            
            NSSet *tags = [beacon valueForKey:@"tags"];
            NSMutableArray *tagsArray = [[NSMutableArray alloc] init];
            for (NSNumber* num in tags) {
                [tagsArray addObject:num];
            }
            [dict setObject:tagsArray forKey:@"tags"];
            
            NSDate *unknownTimer = [beacon valueForKey:@"unknownTimer"];
            [dict setValue:[dateFormatter stringFromDate:unknownTimer] forKey:@"unknownTimer"];
            
            NSDate *lastUpdated = [beacon valueForKey:@"lastUpdated"];
            [dict setValue:[dateFormatter stringFromDate:lastUpdated] forKey:@"lastUpdated"];
            NSDate *lastChanged = [beacon valueForKey:@"lastChanged"];
            [dict setValue:[dateFormatter stringFromDate:lastChanged] forKey:@"lastChanged"];
            
//            [dict setValue:[beacon valueForKey:@"timeLocationMetrics"] forKey:@"timeLocationMetrics"];
            [dict setValue:[beacon valueForKey:@"eddystoneNamespaceID"] forKey:@"eddystoneNamespaceID"];
            [dict setValue:[beacon valueForKey:@"eddystoneInstanceID"] forKey:@"eddystoneInstanceID"];
            [dict setValue:[beacon valueForKey:@"eddystoneURL"] forKey:@"eddystoneURL"];
            [dict setValue:[beacon valueForKey:@"power"] forKey:@"power"];
            [dict setValue:[beacon valueForKey:@"telemetry"] forKey:@"telemetry"];
            [dict setValue:[beacon valueForKey:@"accuracy"] forKey:@"accuracy"];
            
            
            [dict setValue:[beacon valueForKey:@"beaconId"] forKey:@"beaconId"];
            [dict setValue:[beacon valueForKey:@"name"] forKey:@"name"];
            [dict setValue:[beacon valueForKey:@"device_name"] forKey:@"device_name"];
            [dict setValue:[beacon valueForKey:@"batt"] forKey:@"batt"];
            [dict setValue:[beacon valueForKey:@"abdescription"] forKey:@"abdescription"];
            [dict setValue:[beacon valueForKey:@"lat"] forKey:@"lat"];
            [dict setValue:[beacon valueForKey:@"lng"] forKey:@"lng"];
            [dict setValue:[beacon valueForKey:@"locationId"] forKey:@"locationId"];
            
            NSDictionary *beaconLocation = [beacon valueForKey:@"location"];
            
            NSMutableDictionary *location = [[NSMutableDictionary alloc] init];
            [location setValue:[beaconLocation valueForKey:@"locationId"] forKey:@"locationId"];
            [location setValue:[beaconLocation valueForKey:@"name"] forKey:@"name"];
            [location setValue:[beaconLocation valueForKey:@"country"] forKey:@"country"];
            [location setValue:[beaconLocation valueForKey:@"city"] forKey:@"city"];
            [location setValue:[beaconLocation valueForKey:@"zip"] forKey:@"zip"];
            [location setValue:[beaconLocation valueForKey:@"street"] forKey:@"street"];
            [location setValue:[beaconLocation valueForKey:@"street_number"] forKey:@"street_number"];
            [location setValue:[beaconLocation valueForKey:@"lat"] forKey:@"lat"];
            [location setValue:[beaconLocation valueForKey:@"lng"] forKey:@"lng"];
            [dict setObject:location forKey:@"location"];
            
            [dict setValue:[beacon valueForKey:@"range"] forKey:@"range"];
            [dict setValue:[beacon valueForKey:@"freq"] forKey:@"freq"];
            [dict setValue:[beacon valueForKey:@"fwver"] forKey:@"fwver"];
            [dict setValue:[beacon valueForKey:@"hwver"] forKey:@"hwver"];
            [dict setValue:[beacon valueForKey:@"sysid"] forKey:@"sysid"];
            [dict setValue:[beacon valueForKey:@"encrypted"] forKey:@"encrypted"];
            [dict setValue:[beacon valueForKey:@"rev"] forKey:@"rev"];
            [dict setValue:[beacon valueForKey:@"sync_required"] forKey:@"sync_required"];
            [dict setValue:[beacon valueForKey:@"have_new_config"] forKey:@"have_new_config"];
            [dict setValue:[beacon valueForKey:@"tlm_sync_required"] forKey:@"tlm_sync_required"];
            [dict setValue:[beacon valueForKey:@"config_uuid"] forKey:@"config_uuid"];
            [dict setValue:[beacon valueForKey:@"config_major"] forKey:@"config_major"];
            
            NSDate *lastRefreshed = [beacon valueForKey:@"lastRefreshed"];
            [dict setValue:[dateFormatter stringFromDate:lastRefreshed] forKey:@"lastRefreshed"];
            
            [dict setValue:[beacon valueForKey:@"refreshRate"] forKey:@"refreshRate"];
            [results addObject:dict];
        }
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
        [result setKeepCallbackAsBool:YES];
        for (NSString *callbackId in rangeBeaconsListeners) {
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    };
}

-(void (^)(NSError *error)) createErrorHandler {
    return ^(NSError *error) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:error.localizedDescription];
        [result setKeepCallbackAsBool:YES];
        if (errorCallbackId.length > 0) [self.commandDelegate sendPluginResult:result callbackId:errorCallbackId];
    };
}

-(void (^)(NSArray *coupons)) createCouponHandler {
    return ^(NSArray *coupons) {
        NSMutableArray * results = [[NSMutableArray alloc] init];
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"dd-MM-yyy HH:mm:ss ZZZ"];
        for ( int i = 0, size = (int) coupons.count; i< size; i++) {
            NSMutableDictionary* coupon = [coupons objectAtIndex:i];
            NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
            [dict setValue:[coupon valueForKey:@"title"] forKey:@"name"];
            [dict setValue:[coupon valueForKey:@"uuid"] forKey:@"couponId"];
            [dict setValue:[coupon valueForKey:@"message"] forKey:@"message"];
            [dict setValue:[coupon valueForKey:@"couponDescription"] forKey:@"description"];
            [dict setValue:[coupon valueForKey:@"path"] forKey:@"path"];
            [dict setValue:[coupon valueForKey:@"action"] forKey:@"action"];
            [dict setValue:[coupon valueForKey:@"contentState"] forKey:@"contentState"];
            [dict setValue:[coupon valueForKey:@"contentType"] forKey:@"type_id"];
            [dict setValue:[coupon valueForKey:@"beaconUmm"] forKey:@"beaconId"];
            [dict setValue:[coupon valueForKey:@"couponState"] forKey:@"state"];
            
            NSDate *createTime = [coupon valueForKey:@"createTime"];
            [dict setValue:[dateFormatter stringFromDate:createTime] forKey:@"createTime"];
            NSDate *expirationDate = [coupon valueForKey:@"expirationDate"];
            [dict setValue:[dateFormatter stringFromDate:expirationDate] forKey:@"expires"];
            [results addObject:dict];
        }
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
        [result setKeepCallbackAsBool:YES];
        for (NSString *callbackId in couponsListeners) {
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    };
}

#pragma mark - OnyxBeaconCouponDelegate Methods
- (void)didRangeBeacons:(NSArray *)beacons inRegion:(OBBeaconRegion *)region {
    _rangeBeaconsHandler(beacons, region);
}

- (void)locationManagerDidEnterRegion:(CLRegion *)region{

    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@\");", @"window.cordova.plugins.Ble.locationManagerDidEnterRegion", region];
    [self.commandDelegate evalJs:jsString];
    
}

- (void)locationManagerDidExitRegion:(CLRegion *)region{
    
    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@\");", @"window.cordova.plugins.Ble.locationManagerDidExitRegion", region];
    [self.commandDelegate evalJs:jsString];
    
}

- (void)didReceiveContent:(NSArray *)coupons {
    _couponHandler(coupons);
}

- (void)didRequestInfo:(OBContent *)content inViewController:(UIViewController *)viewController {
    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@,%@\");", @"window.cordova.plugins.Ble.didRequestInfo", content,viewController.restorationIdentifier];
    [self.commandDelegate evalJs:jsString];
}

- (void)contentOpened:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];

    
    
    [[OnyxBeacon sharedInstance] contentOpened:content];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"contentOpened Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getTags:(CDVInvokedUrlCommand *)command {
    NSArray * result  = [[OnyxBeacon sharedInstance] getTags];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsArray:result];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)getSelectedTags:(CDVInvokedUrlCommand *)command {
    NSArray * result  = [[[OnyxBeacon sharedInstance] getSelectedTags] allObjects];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsArray:result];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)clearCoupons:(CDVInvokedUrlCommand *)command {
    [[OnyxBeacon sharedInstance] clearCoupons];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"clearCoupons Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setTags:(CDVInvokedUrlCommand *)command {
    
    NSSet *set = [[NSSet alloc] init];
    [set setByAddingObjectsFromArray:command.arguments];
    [[OnyxBeacon sharedInstance] setTags:set];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"setTags Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void)contentTapped:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];
    [[OnyxBeacon sharedInstance] contentTapped:content];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"contentTapped Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showCoupon:(CDVInvokedUrlCommand *)command {
    OBContent *content = [[OBContent alloc] init];
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    UIViewController *rootViewController = window.rootViewController;

    // NSString *couponStr = [command.arguments objectAtIndex:0];
    // NSData *couponData = [couponStr dataUsingEncoding:NSUTF8StringEncoding];
    // NSError *jsonError;
    // NSDictionary *couponJSON = [NSJSONSerialization JSONObjectWithData:couponData options:NSJSONReadingMutableContainers error:&jsonError];
    // if (jsonError != nil) {
    //     CDVPluginResult* pluginResult = [CDVPluginResult
    //                                      resultWithStatus:CDVCommandStatus_ERROR
    //                                      messageAsString: jsonError.localizedDescription];
        
    //     [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    //     return;
    // }

    NSDictionary *couponJSON = [command.arguments objectAtIndex:0];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"dd-MM-yyy HH:mm:ss ZZZ"];
    
    [content setValue:[couponJSON valueForKey:@"name"] forKey:@"title"];
    [content setValue:[couponJSON valueForKey:@"couponId"] forKey:@"uuid"];
    [content setValue:[couponJSON valueForKey:@"message"]forKey:@"message"];
    [content setValue:[couponJSON valueForKey:@"description"] forKey:@"couponDescription"];
    [content setValue:[couponJSON valueForKey:@"path"] forKey:@"path"];
    [content setValue:[couponJSON valueForKey:@"action"] forKey:@"action"];
    [content setValue:[couponJSON valueForKey:@"beaconId"] forKey:@"beaconUmm"];
    
    NSDate *createTimeDate = [dateFormatter dateFromString:[couponJSON valueForKey:@"createTime"]];
    [content setValue:createTimeDate forKey:@"createTime"];
    
    NSDate *expirationDate = [dateFormatter dateFromString:[couponJSON valueForKey:@"expirationDate"]];
    [content setValue:expirationDate forKey:@"expirationDate"];
    
    NSNumber *state = [couponJSON valueForKey:@"state"];
    switch (state.intValue) {
        case 1:
            [content setContentState:ContentStateSent];
            break;
        case 2:
             [content setContentState:ContentStateUnread];
            break;
        case 3:
             [content setContentState:ContentStateRead];
            break;
        case 4:
             [content setContentState:ContentStateSaved];
            break;
        case 5:
             [content setContentState:ContentStateArchived];
            break;
            
        default:
             [content setContentState:ContentStateInit];
            break;
    }
    
    NSNumber *type = [couponJSON valueForKey:@"type_id"];
    switch (type.intValue) {
        case 1:
            [content setContentType:ContentTypeImage];
            break;
        case 2:
            [content setContentType:ContentTypeWeb];
            break;
        case 3:
            [content setContentType:ContentTypeText];
            break;
            
        default:
            [content setContentType:ContentTypeImage];
            break;
    }

    UIViewController *vc = [[OnyxBeacon sharedInstance] viewControllerForContent:content];
    UINavigationController *nc = [[UINavigationController alloc] initWithRootViewController:vc];
    nc.navigationBar.tintColor = [UIColor whiteColor];
    nc.navigationBar.barTintColor = [UIColor blackColor];
    [nc.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor whiteColor]}];
    [rootViewController presentViewController:nc
                                            animated:YES completion:nil];
    
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"showCoupon Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)viewControllerForContent:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];
    UIViewController *c = [[OnyxBeacon sharedInstance] viewControllerForContent:content];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: c.restorationIdentifier];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)deleteContent:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];
    [[OnyxBeacon sharedInstance] deleteContent:content];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"deleteContent Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


#pragma mark - Main OnyxBeacon

- (void) version: (CDVInvokedUrlCommand *)command {
    
    [[OnyxBeacon sharedInstance] version];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"Version Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

- (void)sendUserMetrics : (CDVInvokedUrlCommand *)command {
    NSMutableDictionary* user = [command.arguments objectAtIndex:0];

    [[OnyxBeacon sharedInstance] sendUserMetrics:user];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"sendUserMetrics Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)registerForPushNotificationWithDeviceToken : (CDVInvokedUrlCommand *)command {
    NSData* token = [[command.arguments objectAtIndex:0] dataUsingEncoding:NSUTF8StringEncoding];
    NSString* provider = [command.arguments objectAtIndex:0];
    
    [[OnyxBeacon sharedInstance] registerForPushNotificationWithDeviceToken:token forProvider:provider handler:^(NSDictionary *d, NSError *e) {
        
    }];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"registerForPushNotificationWithDeviceToken Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)sendPushNotificationProviderDeviceToken : (CDVInvokedUrlCommand *)command {
    NSString* token = [command.arguments objectAtIndex:0] ;
    
    [[OnyxBeacon sharedInstance] sendPushNotificationProviderDeviceToken:token];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"sendPushNotificationProviderDeviceToken Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)sendReport :(CDVInvokedUrlCommand *)command {
    
    NSData* data = [[command.arguments objectAtIndex:0] dataUsingEncoding:NSUTF8StringEncoding];
    NSString* reporter = [command.arguments objectAtIndex:1];
    NSString* message = [command.arguments objectAtIndex:2];
    
    [[OnyxBeacon sharedInstance] sendReport:data reporter:reporter message:message handler:^(NSError *error) {

    }];
    
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"sendLogReport Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)viewControllerForTags:(CDVInvokedUrlCommand *)command
{
    UIViewController *c =  [[OnyxBeacon sharedInstance] viewControllerForTags];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: c.restorationIdentifier];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    
}

- (void)onyxBeaconError:(NSError *)error {
    _errorHandler(error);
}

-(void) initSDK:(CDVInvokedUrlCommand *)command {
    NSString* SA_CLIENTID = [self.settings cordovaSettingForKey:@"com-cordova-ble-clientId"];
    NSString* SA_SECRET = [self.settings cordovaSettingForKey:@"com-cordova-ble-secret"];
    
    CDVPluginResult* pluginResult;
    
    if ([SA_CLIENTID isEqualToString:@""] || SA_CLIENTID.length <= 0){
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"empty client id"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    if ([SA_SECRET isEqualToString:@""] || SA_SECRET.length <= 0){
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"empty secret"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
// [self.commandDelegate runInBackground:^{
        [[OnyxBeacon sharedInstance] requestAlwaysAuthorization];
        [[OnyxBeacon sharedInstance] startServiceWithClientID:SA_CLIENTID secret:SA_SECRET];
        [[OnyxBeacon sharedInstance] setContentDelegate:self];
        [[OnyxBeacon sharedInstance] setDelegate:self];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                         messageAsString: @"startServiceWithClientID Invoked"];
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
// }];  
    
}

-(void) stop:(CDVInvokedUrlCommand *)command {
    
    [[OnyxBeacon sharedInstance] resetService];
    
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"resetService Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)enterBackground:(CDVInvokedUrlCommand *)command
{
    [[OnyxBeacon sharedInstance] didEnterBackground];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"didEnterBackground Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)enterForeground:(CDVInvokedUrlCommand *)command
{
    [[OnyxBeacon sharedInstance] willEnterForeground];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"didEnterBackground Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

@end