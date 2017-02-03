module.exports = function (ctx) {
    // make sure android platform is part of build
    if (ctx.opts.cordova.platforms.indexOf('android') < 0) {
        return;
    }
    var fs = ctx.requireCordovaModule('fs'),
        path = ctx.requireCordovaModule('path'),
        deferral = ctx.requireCordovaModule('q').defer();

    var platformRoot = path.join(ctx.opts.projectRoot, 'platforms/android');
    var manifestFileLocation = path.join(platformRoot, 'AndroidManifest.xml');

    //Modify AndroidManifest.xml
    fs.readFile(manifestFileLocation, 'utf-8', function(err, data){
        var newManifest = data;
        //add tools namespace
        if (data.indexOf('http://schemas.android.com/tools') === -1) {
            newManifest = newManifest.replace('xmlns:android','xmlns:tools=\"http://schemas.android.com/tools\"\nxmlns:android');
        }
        
        //add tools:replace
        if (newManifest.indexOf('tools:replace=\"android:icon\"') === -1) {
            newManifest = newManifest.replace('<application','<application tools:replace=\"android:icon\" ');
        }

        fs.writeFile(manifestFileLocation, newManifest, 'utf-8', function(err){
            if (err) throw err;
            console.log('AndroidManifest.xml complete');
            deferral.resolve();
        })
    })
    

    return deferral.promise;
};