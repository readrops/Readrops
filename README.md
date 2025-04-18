<p align="center">
    <img src="fastlane/metadata/android/en-US/images/icon.png" width=180>
</p>

<h1 align="center"><b>Readrops</b></h1>

<p align="center">
<a href="https://github.com/readrops/Readrops/actions"><img src="https://github.com/readrops/Readrops/actions/workflows/android.yml/badge.svg?branch=develop"></a>
<a href="https://codecov.io/gh/readrops/Readrops"><img src="https://codecov.io/gh/readrops/Readrops/branch/develop/graph/badge.svg?token=229PNPQPMM"></a>
<a href="https://hosted.weblate.org/engage/readrops/"><img src="https://hosted.weblate.org/widgets/readrops/-/strings/svg-badge.svg"/></a>

<h4 align="center">Readrops is a multi-services RSS client for Android. Its name is composed of "Read" and "drops", where drops are articles in an ocean of news.</h4>

<p align="center">
    <a href="https://play.google.com/store/apps/details?id=com.readrops.app"><img src="images/google-play-badge.png" width=250></a>
    <a href="https://f-droid.org/en/packages/com.readrops.app/"><img src="images/fdroid-badge.png" width=250></a>
</p>

# Features

- Local RSS parsing (RSS1, RSS2, ATOM, JSONFeed)
- External services:
  - FreshRSS
  - Nextcloud News
  - Fever API
  - Google Reader API
- Multi-account
- Feeds and folders management (create, update and delete feeds/folders if supported by the service API)
- OPML import/export
- Background synchronisation
- Notifications

# Screenshots

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_1.jpg" width=250> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_2.jpg" width=250> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_3.jpg" width=250> 

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_4.jpg" width=250> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_5.jpg" width=250> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_6.jpg" width=250>

# Licence

This project is released under the GPLv3 licence.

# Develop

During development, you can autofill the app's login form by filling the project's `local.properties` like so:

```properties
debug.<account_type>.login=<login>
debug.<account_type>.password=<password>
debug.<account_type>.url=https\://<your_instance>

# For instance:
debug.nextcloud_news.login=Test user
debug.nextcloud_news.password=1234
debug.nextcloud_news.url=https\://rss.example.com
```

# Donations

[<img src="images/paypal-badge.png" width=250>](https://paypal.me/readropsapp)

Bitcoin address : `bc1qlkzlcsvvtn3y6mek5umv5tc4ln09l64x6y42hr` <br />
Litecoin address : `MTuf45ZvxhMWWo4v8YBbFDTLsFcGtpcPNT`
