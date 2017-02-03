    var argscheck = require('cordova/argscheck'),
        channel = require('cordova/channel'),
        utils = require('cordova/utils'),
        exec = require('cordova/exec'),
        cordova = require('cordova');



    var Ble = function () {
    };

    var Tag = function (id, name, status) {

        this.id = id || "";
        this.name = name || "";
        this.status = status || "";
    };

    var OBContent = function (title, uuid, message, path, action, createTime, contentState, contentType) {


        this.title = title || "";
        this.uuid = uuid || "";
        this.message = message || "";
        this.path = path || "";
        this.action = action || "";
        this.createTime = createTime || "";
        this.contentState = contentState || "";
        this.contentType = contentType || "";


    };



    /**
     * @Platform iOS
     * startServiceWithClientID
     * @param SA_CLIENTID
     * @param SA_SECRET
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */

    Ble.startServiceWithClientID = function (clientid, clientsecret, success, fail) {
        exec(success, fail, 'Ble', 'startServiceWithClientID', [clientid, clientsecret]);
    };

    /**
     * @Platform iOS
     * resetService
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */

    Ble.resetService = function (success, fail) {
        exec(success, fail, 'Ble', 'resetService', []);
    };


    /**
     * @Platform iOS
     * viewControllerForContent
     * @param {OBContent} ob_content
     * @param {Function} successCallback
     * @param {Function} errorCallback
     * @return UIViewController
     */
    Ble.viewControllerForContent = function (ob_content, success, fail) {
        exec(success, fail, 'Ble', 'viewControllerForContent', [ob_content]);
    };

    /**
     * @Platform iOS
     * getTags
     * @param {Function} successCallback
     * @param {Function} errorCallback
     * @return array of tags
     */
    Ble.getTags = function (success, fail) {
        exec(success, fail, 'Ble', 'getTags ', []);
    };


    /**
     * @Platform iOS
     * getSelectedTags
     * @param {Function} successCallback
     * @param {Function} errorCallback
     * @return array of selected tags
     */
    Ble.getSelectedTags = function (success, fail) {
        exec(success, fail, 'Ble', 'getSelectedTags ', []);
    };

    /**
     * @Platform iOS
     * setTags
     * @param {nsset} tags [[id,name],[id,name]]
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setTags = function (tags, success, fail) {
        exec(success, fail, 'Ble', 'setTags ', tags);
    };

    /**
     * @Platform iOS
     * clearCoupons
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.clearCoupons = function (success, fail) {
        exec(success, fail, 'Ble', 'clearCoupons ', []);
    };



    /**
     * @Platform iOS
     * deleteContent
     * @param {OBContent} ob_content
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.deleteContent = function (ob_content, success, fail) {
        exec(success, fail, 'Ble', 'deleteContent', [ob_content]);
    };


    /**
     * @Platform iOS
     * contentOpened
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.contentOpened = function (ob_content, success, fail) {
        exec(success, fail, 'Ble', 'contentOpened ', [ob_content]);
    };

    /**
     * @Platform iOS
     * contentTapped
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.contentTapped = function (ob_content, success, fail) {
        exec(success, fail, 'Ble', 'contentTapped ', [ob_content]);
    };

    /**
     * @Platform iOS
     * showContentInfo
     * @param {OBContent} ob_content
     * @param {UIViewController} viewController
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.showContentInfo = function (ob_content, viewController, success, fail) {
        exec(success, fail, 'Ble', 'showContentInfo ', [ob_content, viewController]);
    };

    /**
     * @Platform iOS
     * getContent
     * @param {Function} successCallback
     * @param {Function} errorCallback
     * @return Content Array
     */
    Ble.getContent = function (success, fail) {
        exec(success, fail, 'Ble', 'getContent', []);
    };

    /**
     * @Platform iOS
     * version
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.version = function (success, fail) {
        exec(success, fail, 'Ble', 'version', []);
    };

    /**
     * @Platform iOS
     * sendUserMetrics
     * @param {NSDictionary} user [{.....}]
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.sendUserMetrics = function (user, success, fail) {
        exec(success, fail, 'Ble', 'sendUserMetrics', [user]);
    };


    /**
     * @Platform iOS
     * registerForPushNotificationWithDeviceToken
     * @param {NSString} token
     * @param {NSString} pushProvider
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.registerForPushNotificationWithDeviceToken = function (token, pushProvider, success, fail) {
        exec(success, fail, 'Ble', 'sendPushNotificationProviderDeviceToken', [token, pushProvider]);
    };

    /**
     * @Platform iOS
     * sendPushNotificationProviderDeviceToken
     * @param {NSDictionary} providerDeviceToken
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.sendPushNotificationProviderDeviceToken = function (token, success, fail) {
        exec(success, fail, 'Ble', 'sendPushNotificationProviderDeviceToken', [token]);
    };

    /**
     * @Platform iOS
     * sendReport
     * @param {NSString} data
     * @param {NSString} reporter
     * @param {NSString} message
     * @param {Function} errorCallback
     */
    Ble.sendReport = function (data, reporter, message, success, fail) {
        exec(success, fail, 'Ble', 'sendReport', [data, reporter, message]);
    };

    /**
     * @Platform iOS
     * viewControllerForTags
     * @param {Function} successCallback
     * @param {Function} errorCallback
     * @return UIViewController
     */
    Ble.viewControllerForTags = function (success, fail) {
        exec(success, fail, 'Ble', 'viewControllerForTags', []);
    };


    /**
     * @Platform iOS
     * requestAlwaysAuthorization
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.requestAlwaysAuthorization = function (success, fail) {
        exec(success, fail, 'Ble', 'requestAlwaysAuthorization', []);
    };


    /**
     * @Platform iOS
     * applicationDidEnterBackground
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.applicationDidEnterBackground = function (success, fail) {
        exec(success, fail, 'Ble', 'applicationDidEnterBackground', []);
    };

    /**
     * @Platform iOS
     * applicationWillEnterForeground
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.applicationWillEnterForeground = function (success, fail) {
        exec(success, fail, 'Ble', 'applicationWillEnterForeground', []);
    };

    /**
     * @Platform iOS
     * setLogger
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setLogger = function (success, fail) {
        exec(success, fail, 'Ble', 'setLogger', []);
    };

    /**
     * @Platform iOS
     * requestWhenInUseAuthorization
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.requestWhenInUseAuthorization = function (success, fail) {
        exec(success, fail, 'Ble', 'requestWhenInUseAuthorization', []);
    };

    /**
     * initSDK
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.initSDK = function (clientId, secret, success, fail) {
        exec(success, fail, 'Ble', 'initSDK', [clientId, secret]);
    };

    /**
     * isBluetoothAvailable
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.isBluetoothAvailable = function (success, fail) {
        exec(success, fail, 'Ble', 'isBluetoothAvailable', []);
    };

    /**
     * startScan
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.startScan = function (success, fail) {
        exec(success, fail, 'Ble', 'startScan', []);
    };

    /**
     * stopScan
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.stopScan = function (success, fail) {
        exec(success, fail, 'Ble', 'stopScan', []);
    };

    /**
     * setBackgroundBetweenScanPeriod
     * @param {int} period
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setBackgroundBetweenScanPeriod = function (period, success, fail) {
        exec(success, fail, 'Ble', 'setBackgroundBetweenScanPeriod', [period]);
    };

    /**
     * setForegroundMode
     * @param {Boolean} is
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setForegroundMode = function (is, success, fail) {
        exec(success, fail, 'Ble', 'setForegroundMode', [is]);
    };

    /**
     * setCouponEnabled
     * @param {Boolean} is
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setCouponEnabled = function (is, success, fail) {
        exec(success, fail, 'Ble', 'setCouponEnabled', [is]);
    };

    /**
     * enableGeofencing
     * @param {Boolean} is
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.enableGeofencing = function (is, success, fail) {
        exec(success, fail, 'Ble', 'enableGeofencing', [is]);
    };
    /**
     * setLocationTrackingEnabled
     * @param {Boolean} is
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setLocationTrackingEnabled = function (is, success, fail) {
        exec(success, fail, 'Ble', 'setLocationTrackingEnabled', [is]);
    };

    /**
     * setAPIContentEnabled
     * @param {Boolean} is
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setAPIContentEnabled = function (is, success, fail) {
        exec(success, fail, 'Ble', 'setAPIContentEnabled', [is]);
    };


    /**
     * enableBluetooth
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.enableBluetooth = function (success, fail) {
        exec(success, fail, 'Ble', 'enableBluetooth', []);
    };


    /**
     * getTags
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.getTags = function (success, fail) {
        exec(success, fail, 'Ble', 'getTags', []);
    };


    /**
     * restartLocationTracking
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.restartLocationTracking = function (success, fail) {
        exec(success, fail, 'Ble', 'restartLocationTracking', []);
    };

    /**
     * logOut
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.logOut = function (success, fail) {
        exec(success, fail, 'Ble', 'logOut', []);
    };

    /**
     * sendGenericUserProfile
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.sendGenericUserProfile = function (profile, success, fail) {
        exec(success, fail, 'Ble', 'sendGenericUserProfile', [profile]);
    };

    /**
     * isInForeground
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.isInForeground = function (success, fail) {
        exec(success, fail, 'Ble', 'isInForeground', []);
    };


    /**
     * isCouponEnabled
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.isCouponEnabled = function (success, fail) {
        exec(success, fail, 'Ble', 'isCouponEnabled', []);
    };

    /**
     * isAPIEnabled
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.isAPIEnabled = function (success, fail) {
        exec(success, fail, 'Ble', 'isAPIEnabled', []);
    };

    /**
     * sendDeviceToken
     * @param {Function} var1
     * @param {Function} var2
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.sendDeviceToken = function (var1, var2, success, fail) {
        exec(success, fail, 'Ble', 'sendDeviceToken', [var1, var2]);
    };

    /**
     * setAuthExtraData
     * @param {Function} var1
     * @param {Function} var2
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setAuthExtraData = function (var1, success, fail) {
        exec(success, fail, 'Ble', 'setAuthExtraData', [var1]);
    };

    /**
     * setAuthExtraData
     * @param {Function} var1, this is JSONARRAY of Tag [[id,name,state],[id,name,state],[id,name,state]]
     * @param {Function} var2
     * @param {Function} successCallback
     * @param {Function} errorCallback
     */
    Ble.setTagsFilterForCoupons = function (tagArrayList, success, fail) {
        exec(success, fail, 'Ble', 'setAuthExtraData', [tagArrayList]);
    };



    /**
     * @Platform iOS
     * didRangeBeacons
     */

    Ble.didRangeBeacons = function (beaconArray, ob_region) {
        console.log(beaconArray);
        console.log(ob_region);
    };

    /**
     * @Platform iOS
     * locationManagerDidEnterRegion
     */
    Ble.locationManagerDidEnterRegion = function (cl_region) {
        console.log(cl_region);

    };


    /**
     * @Platform iOS
     * locationManagerDidEnterRegion
     */
    Ble.locationManagerDidExitRegion = function (cl_region) {
        console.log(cl_region);

    };


    /**
     * @Platform iOS
     * didReceiveContent
     */
    Ble.didReceiveContent = function (couponsArray) {
        console.log(couponsArray);

    };



    /**
     * @Platform iOS
     * didRequestInfo
     */
    Ble.didRequestInfo = function (ob_content, viewcontroller) {
        console.log(ob_content);

    };

    /**
     * @Platform iOS
     * onTagsReceived
     */
    Ble.onTagsReceived = function (tags) {
        console.log(tags);

    };


    /**
     * @Platform iOS
     * onCouponsReceived
     */
    Ble.onCouponsReceived = function (coupan, beacons) {
        console.log(coupan);
        console.log(beacons);


    };



    /**
     * @Platform iOS
     * onCouponsReceived
     */
    Ble.onBluemixCredentialsReceived = function (blueMix) {
        console.log(blueMix);
    };

    /**
     * @Platform iOS
     * didRangeBeaconsInRegion
     */
    Ble.didRangeBeaconsInRegion = function (beacons) {
        console.log(beacons);

    };

    /**
     * @Platform iOS
     * onyxBeaconError
     */
    Ble.onyxBeaconError = function (msg) {
        console.log(msg);

    };


    /**
     * deleteCoupon
     * @param {Function} var1
     * @param {Function} var2
     */
    Ble.deleteCoupon = function (var1, var2) {
        console.log(var1);
        console.log(var2);



    };


    /**
     * markAsTapped
     * @param {Function} var1
     */
    Ble.markAsTapped = function (var1) {
        console.log(var1);

    };

    /**
     * markAsOpened
     * @param {Function} var1
     */
    Ble.markAsOpened = function (var1) {
        console.log(var1);
    };


    Ble.greet = function (name, success, fail) {
        exec(success, fail, "Ble", "greet", [name]);

    };

    if (cordova.platformId === 'android' || cordova.platformId === 'amazon-fireos' || cordova.platformId === 'windowsphone') {

        var channel = require('cordova/channel');

        channel.createSticky('onBlePluginReady');
        channel.waitForInitialization('onBlePluginReady');

        channel.onCordovaReady.subscribe(function () {
            console.log("onBlePluginReady is Fired");
            channel.onBlePluginReady.fire();
        });
    }

    module.exports = Ble;
