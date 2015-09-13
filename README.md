Pop Movies
==========
This mobile app project was made for Udacity Android Nano Degree Program.

Configuration
-------------
This project accesses the https://www.themoviedb.org/ for it's data.

You will need to get your own API key [here](https://www.themoviedb.org/account/signup)
for the code to work. Create the following XML file with your key.

app/src/main/res/values/api_keys.xml

    <?xml version="1.0" encoding="utf-8"?>

    <resources>
        <string name="tmdb_api_key">[YOUR KEY HERE]</string>
    </resources>


Also this project uses retro-lambda to backport Java 8 lambda syntax.  You will
need to install Java 8 and update the location in your app's build.gradle file.

    retrolambda {
        jdk '[JDK LOCATION]'
        defaultMethods true
    }
