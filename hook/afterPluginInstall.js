module.exports = function (ctx) {
    // make sure android platform is part of build
    if (ctx.opts.cordova.platforms.indexOf('android') < 0) {
        return;
    }
    var fs = ctx.requireCordovaModule('fs'),
        path = ctx.requireCordovaModule('path'),
        deferral = ctx.requireCordovaModule('q').defer();

    var platformRoot = path.join(ctx.opts.projectRoot, 'platforms/android');
    var gradleFileLocation = path.join(platformRoot, 'build.gradle');
    var manifestFileLocation = path.join(platformRoot, 'AndroidManifest.xml');

    //Modify build.gradle
    fs.readFile(gradleFileLocation, 'utf-8', function (err, data) {
        if (err) throw err;
        var newValue = data;

        //add classpath
        if (data.indexOf('io.realm:realm-gradle-plugin') === -1) {
            newValue = newValue.replace('classpath', 'classpath \'io.realm:realm-gradle-plugin:2.1.0\'\nclasspath');
        }

        //add repository
        if (newValue.indexOf('mavenCentral()') === -1) {
            newValue = newValue.replace('jcenter()','mavenCentral()\njcenter()')
        }
        

        fs.writeFile(gradleFileLocation, newValue, 'utf-8', function (err) {
            if (err) throw err;
            console.log('build.gradle complete');

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
            
        });
    });

    return deferral.promise;
};