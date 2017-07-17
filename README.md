# cordova-plugin-onyxble
This plugin is built using Onyx SDK version 2.2.2 for iOS and version 2.5.2 for Android.


## Get Started
- Create app in [Onyx admin dashboard](https://connect.onyxbeacon.com/admin/app/create)
- Install the plugin
- Define the clientID and secret

## Installing the plugin
```
cordova plugin add https://github.com/kadekParwanta/cordova-plugin-onyxble --variable PACKAGE_NAME="com.kadek.onyxreceiver"
```

## Define the clientId and secret
```xml
<widget id="com.kadek.onyxreceiver" version="2.0.0" xmlns="http://www.w3.org/ns/widgets" xmlns:cdv="http://cordova.apache.org/ns/1.0">
  <preference name="com-cordova-ble-clientId" value="ADD_YOUR_CLIENTID" />
  <preference name="com-cordova-ble-secret" value="ADD_YOUR_SECRET" />
</widget>
```
## Requirements

- Minimum OS Target (Android 4.3, iOS 7)

## Usage
The plugin creates the object `window.ble`.
```Javascript
var ble = window.ble;
if (!ble) {
    $scope.ErrorCode = "1";
    $scope.ErrorMessage = "Ble module is not available";
    return;
}

ble.onWebRequested(function(info){
    console.log('- onWebRequested ' + info);
});

ble.didRangeBeaconsInRegion(function(beacons){
    console.log('- didRangeBeaconsInRegion ' + beacons);
});

ble.onTagsReceived(function(tags){
    console.log('- onTagsReceived ' + tags);
});

ble.initSDK(function(success){
    console.log('success');
}, function(err){
    $scope.ErrorCode = "2";
    $scope.ErrorMessage = "Failed to init sdk - " + err;
    console.log('failed');
});

```
