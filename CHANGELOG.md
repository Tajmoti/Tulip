## [Unreleased]
### Changed
- Transparent status bar on Library screen
- Preselect last played seasons when enterin TV show screen

## [0.12.0] - 2021-11-07
### Added
- Support for Voe.SX streaming site
- Support for TheVideo.me streaming site

## [0.11.0] - 2021-11-06
### Changed
- Fixed infinite loading in case there are no streams available for the selected episode or movie
- Improve support for grabbing links from Kinox.to

## [0.10.1] - 2021-10-22
### Changed
- Fixed issue where skipping 10 seconds forward would restart the video playback

## [0.10.0] - 2021-10-20
### Added
- *Resume playing* feature for TV shows
- *Next episode* button in video player for TV shows
- Picture-in-picture mode support
### Changed
- Increase thumb size on seekbar in video player
- Search results now appear as they load instead of waiting for the last one before they are shown
- Library and TV show UI rework

## [0.9.1] - 2021-09-25
### Changed
- Fixed episode list not keeping its state when returning from another screen
- Fixed last item in library not disappearing from library UI when removed
- Close video player when the video is done playing
- Don't show irrelevant video player UI elements during loading
- Fixed weird video player UI state when switching sources
- Fixed play button not visible anymore if buffering occurs while the player is paused
- Fixed missing ':' between hours and minutes in video player
- Fixed issue where the video is restarted when returning from another application
- Don't hide player UI automatically if the video is buffering
- Increased size of the player back button and source selection exit button

## [0.9.0] - 2021-09-20
### Added
- Option to disable subtitles once selected
### Changed
- Fix subtitle delay calculation causing incorrect delay to be applied
- Improve subtitle selection UI

## [0.8.0] - 2021-09-20
### Added
- Custom video player with support for changing streams mid-play and subtitles
### Changed
- Fixed streams from Primewire sometimes loaded incorrectly
- Fixed error video played when streaming from streamzz.to

## [0.7.0] - 2021-09-07
### Changed
- Fixed movies from Kinox.to not working
- Add streams to stream result list as they load instead of all at once
- Speed up video link loading from streamzz.to
- Don't expand search view on app start

## [0.6.0] - 2021-09-02
### Added
- Support for searching items in the TMDB database
- Slovak translation
### Changed
- Fixed search progress indicator disappearing after deleting part of query mid-search
- Always use dark theme
- Fixed occasional crashes when loading new data
- Reworked TV Show UI from tabs to expandable list items and implemented library of favorite items
- When a direct stream fails to load, allow the user to open the original page in a browser
- Fixed some streaming sites not being loaded correctly from Kinox.to

## [0.5.0] - 2021-08-17
### Changed
- Speed up video link loading by loading video links on demand

## [0.4.0] - 2021-08-10
### Changed
- Decrease the delay from typing a letter to search start to 500ms
### Added
- Better error handling UI including a retry option

## [0.3.0] - 2021-08-09
### Changed
- Fix black text on dark background when dark mode is used
- Fix searching from multiple sources being performed serially
- UI loading speed improvements
- Searching is now done as the user types

## [0.2.0] - 2021-08-08
### Added
- Video downloading
- Kinox.to support
- Support for multiple content languages
- Support for episode numbers including specials

## [0.1.0] - 2021-07-30
### Added
- Ability to search and play TV shows and movies
- Support for primewire.ag

[Unreleased]: https://github.com/Tajmoti/Tulip/tree/main
[0.10.0]: https://github.com/Tajmoti/Tulip/tree/v0.10.0
[0.9.1]: https://github.com/Tajmoti/Tulip/tree/v0.9.1
[0.9.0]: https://github.com/Tajmoti/Tulip/tree/v0.9.0
[0.8.0]: https://github.com/Tajmoti/Tulip/tree/v0.8.0
[0.7.0]: https://github.com/Tajmoti/Tulip/tree/v0.7.0
[0.6.0]: https://github.com/Tajmoti/Tulip/tree/v0.6.0
[0.5.0]: https://github.com/Tajmoti/Tulip/tree/v0.5.0
[0.4.0]: https://github.com/Tajmoti/Tulip/tree/v0.4.0
[0.3.0]: https://github.com/Tajmoti/Tulip/tree/v0.3.0
[0.2.0]: https://github.com/Tajmoti/Tulip/tree/v0.2.0
[0.1.0]: https://github.com/Tajmoti/Tulip/tree/v0.1.0
