# v2.0.1

- Make Timeline tab filters persistent (#138)
- Change Timeline tab order field default value (#202)
- Fix crash when adding a Fever API account (#200)
- Be less strict with feed and folder names (#206)

# v2.0

- Restore swipe to mark as read (#188)
- Restore Ordering by article id in Timeline tab
- Improve OPML file picker filtering (#195)
- Translation updates
- See previous beta versions to get full changelog since v1.3

# v2.0-beta02

- Fix migration issues from v1.3 and older (especially for F-Droid builds)
- Make Preferences screen scrollable (#190)
- Fix wrong translation in RadioButtonPreferenceDialog (#185)
- Translation updates

# v2.0-beta01

## General

- 🆕 design:
  - 🆕 Material3: Readrops implements last material design system version 
  - 🆕 Bottom bar navigation: you can now navigate to feeds and account management way more easily, with 4 tabs in total:
    - Timeline 
    - Feeds 
    - Account 
    - More 
  - Timeline tab:
    - 🆕 Article size: you can now choose among three article sizes: compact, regular and large 
    - 🆕 You can now show only articles less than 24h old 
    - 🆕 Mark all articles as read FAB: the floating action button now lets you mark all articles read, taking into account the current filter, replacing opening new feed activity action 
    - 🆕 Mark articles read on scroll: an option is now available to mark items read on scroll 
    - 🆕 Drawer: hide folders and feeds without unread articles 
    - 🆕 Local account: sync now respects the current filter and will only synchronize affected feeds 
  - Feeds Tab:
    - 🆕 Feeds and folder management have been merged into a single screen 
  - Account Tab:
    - 🆕 Add, manage and remove any account from Account Tab 
  - More Tab:
    - 🆕 This new screen gathers some app infos, parameters, open source libraries and a donation dialog 
  - Articles screen:
    - The global UI has been improved with a new title layout 
    - 🆕 Action bottom bar: you now have access to a collapsable bottom bar containing the following actions:
      - Mark as read/non read 
      - Add to favorites/remove from favorites 
      - Share 
      - Open in external navigator/navigator view 
    - 🆕 A new font, Inter is used for the article content 
    - 🆕 Some html tags look have been improved:
      - blockquote 
      - figure/figcaption 
      - iframe 
      - table
    - "Open in" option has been reduced to two options: navigator view and external navigator 
- 🆕 FEVER API implementation, should work with any provider which supports it 
- Migrate to Nextcloud News API 1.3 
- 🆕 Follow system theme option (default)
- 🆕 Option to disable battery optimization for background synchronization
- Add support for new Android versions until Android 14 (API 34)

## Technical

- The UI has been entirely rewritten in Kotlin using Jetpack Compose, moving from old traditional view system
- All other Java parts have also been rewritten in Kotlin, including API implementations, repositories, etc
- RXJava was replaced by Kotlin coroutines and flows
- Migrate to Gradle Kotlin DSL
- Migrate dependencies to Version Catalog
- 🆕 Support user certificates

# v1.3.1

- FreshRSS : Fix items being fav unintentionally
- FreshRSS : Fix 401 error when synchronising for the second time

# v1.3.0

- New local RSS parser, much reliable
- New external navigator view for items (Custom tabs)
- FreshRSS and Nextcloud News favorites
- FreshRSS read state synchronisation
- New folder view when clicking on a drawer expandable item (#56)

# v1.2.1

- Accept null value for Nextcloud News feed attribute folderId #87
- Fix local RSS url parsing 

# v1.2.0

- Adaptive icon
- Background synchronisation
- Notifications
- FreshRSS and Nextcloud News synchronisation speed improvements
- Add feed direct share action
- Improve some html tags look
- Diplay image title or alt as caption (#63)
- Local RSS fixes (#70, #71)
- Other bug fixes and UI improvements

# v1.1.4

- Fix app crash when using an account url without http scheme #55
- Fix FreshRSS folder name parsing #61
- Fix feeds being deleted when adding a new feed with a NC News account #59

# v1.1.3

- Fix crash for API < 24 #51
- Fix item read state not syncing with Nextcloud News #49

# v1.1.2

- Fix opml not working in release version
- Fix account url not being changed when text field in add account activity was edited

# v1.1.1

Request write storage permission before downloading image.

# v1.1.0

- OPML import/export for local account
- Dark theme
- Share or download item image
- Open item in webview
- Minor bug fixes and improvements

# v1.0.2.2

Disable Proguard as it makes fail some functionalities.

# v1.0.2.1

Fix a crash related to Proguard Rules.

# v1.0.2

- Add swipe background to main list items
- Add preference to parse a fixed number of items when adding a local feed
- Change feed/folders way to interact
- Minor bug fixes and improvements

# v1.0.1

# v1.0 Initial release

- Local RSS parsing 
- RSS 2.0, ATOM and JSON formats support 
- Multiple accounts 
- Feeds and folders management (create, update and delete feeds/folders if your service API supports it)
- Nextcloud news support 
- FreshRSS support
