# v2.1.0

This release focuses mainly on QOL improvements. You will find among them long time requested features. I hope in the future to be able to deliver new releases in less than six months.

## New features

- 🆕 Pager in article screen: You can now swipe left/ right to go to the previous/next article without going back to the main list #62
- 🆕 New feed screen. A new screen replaces the dialog to add a feed.
  - All declared RSS resources in a website will appear in a list. You will be able to select only the ones you would like to add
  - You can now directly choose a folder for each feed to add
- 🆕 Feed color screen: You can now change each feed color in a new screen. #104
- 🆕 Per feed open parameter: you can now choose for each feed to open its articles in the article screen or directly in the external view. #105, #125
- 🆕 Initial tablet mode: left navigation bar and permanent navigation drawer in Timeline tab. More improvements will come for a 100% big screen support
- 🆕 Parameter to launch synchronization at startup #158
- 🆕 Parameter to set the default drawer filter at launch
- 🆕 Two parameters to customize left and right swipe actions in Timeline tab #117
- 🆕 Google Reader API which powers FreshRSS support can now be used as a standalone API
- 🆕 Modify shared article text with a custom template
- 🆕 Android 15 support

## Improvements

- Local account:
  - Synchronization speed improvements
  - Icons quality improved (currently only new icons will have better quality, a global option to reload all icons will come in the future)
  - Display feed banner in feed bottom sheet (currently new feeds only)
  - Improve media support for RSS2 and ATOM (Youtube...)
  - Date parsing improvements
- OPML import speed improvements
- Display feed notifications parameter in feed bottom sheet
- Make login checks less restrictive #193
- Move Folders beginning with \_ on top #78
- Improve feed color handling #172
- Autofill managers are now usable in login screen #253
- Translation updates

## Fixes

- Downloaded image now appears in media gallery #226
- Various image share/download fixes in article screen
- Fix scroll jump in article screen when touching the screen for the first time #184
- Fix a rare case where local parsing could fail #246
- Fix Nextcloud News feed creation where all local feeds could be deleted
- Fix crash when opening empty OPML file #244, #245
- Fix hide feeds without new items parameter for some accounts #255
- Fix crash when no item link was provided #247

## Contributions

- Thanks to all translators who worked on Weblate! #283, #274, #256, #241
- FreshRSS casing #230 by @Alkarex
- Fix endpoint slash #231 by @Alkarex
- Fix formatting of plain text items #236 by @equeim
- Two minor improvements for the German localization #237 by @BorisBrock
- Integrate login screen with autofill managers (#253) by @christophehenry
- UX: increase DrawerFolderItem's expand button to avoid missclicks (#257) by @christophehenry
- Corrects a gramatical form (#248) by @StuntsPT
- Customize shared text using template setting (#254) by @christophehenry
- Fix gap above bottom navigation bar (#266) by @equeim
- Fix colors on More tab (#270) by @equeim
- Fix HTML parsing (#273) by @equeim
- Share Intent template: Add french typography filter + improve template dialog (#269) by @christophehenry
- Add mecanism to deduce feeds location in special cases (#272) by @christophehenry
- Add managed punctuation marks to fr_typo (#279) by @christophehenry
- Allow sourcing account credentials from local.properties during development (#275) by @christophehenry
- Fix unread feed selection in FoldersAndFeedsQueryBuilder (#276) by @christophehenry

# v2.0.3
- Fix Fever API compatibility with TinyTiny RSS and yarr, should also fix other providers (#228 + #229) 
- Fix Nextcloud News item duplicates when syncing which would made the app unusable
- Fix Nextcloud News item parsing: items with no title will be ignored

# v2.0.2
- Fix crash when opening app from a notification (#223)
- Fix Fever API synchronization error (#228)

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
