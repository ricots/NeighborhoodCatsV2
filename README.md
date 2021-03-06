# ![](https://ga-dash.s3.amazonaws.com/production/assets/logo-9f88ae6c9c3871690e33280fcf557f33.png) Neighborhood Cats (Android) Readme :cat:

![](https://github.com/roberrera/NeighborhoodCats/blob/master/NeighborhoodCats/Screenshots/Neighborhood_Cats_feature.jpg)

Neighborhood Cats is a photo-sharing app for Android that lets you keep your cat sightings organized and accessible, while retaining the ability to show the world the adorable (or hideous) kitty you've encountered.

In the neighborhood my wife and I used to live in we would pass stray cats as well as domesticated cats sitting in windows. We would play a game in which we would count how many cats we spotted each day, and I would take photos. But I'd often forget on which block I spotted each cat, and the photos would get buried in my gallery as I took other (non-cat) photos. After we moved to a new neighborhood, I set out to build an app that records these cat sightings and allows you to better remember your feline friends and where you saw them.

My hope is that this app will give you some joy as you photograph the cats in your neighborhood. You can add a name for the cat and notes, and the cats show up as a list.

### Things you can do in this app:
 - Open the camera to take a photo of a cat.
 - Add a photo of a cat from your phone's photo gallery.
 - Add a name and description for your cat.
 - Get the location for where you took the photo/saved a new entry.
 - See your saved cats' locations on a map.
 - Swipe away a cat on your list to delete the entry.
 - Share an entry to an app on your phone (shares the photo, cat name, and cat notes).
 - More features are planned! Stay tuned! (To suggest a feature please email me at robert.errera.developer@gmail.com)

### APIs, SDKs, Libraries, & open source code used:
 - Camera API
 - Google Maps API
 - Google Analytics API
 - [Picasso library](http://square.github.io/picasso/)
 - Bruno R. Nunes' [SwipableRecyclerView](https://github.com/brnunes/SwipeableRecyclerView)
 - Ralf Gehrer's [AndroidCameraUtil](https://github.com/ralfgehrer/AndroidCameraUtil) 
 - RecyclerView / CardView

### Known Bugs:
 - The first photo added does not reliably appear on the list. (All entries added after the first one are added without a problem.)
 - If the image is an older image being selected via Google Photos, the image will not be loaded because it has to be downloaded first (so there is no image file path to load when the app is looking for the file path).

*Now available on the [Google Play Store!](https://play.google.com/store/apps/details?id=com.roberterrera.neighborhoodcats)*

![](https://github.com/roberrera/NeighborhoodCats/blob/master/NeighborhoodCats/Screenshots/device-2016-04-04-220043(75%20pct).png)
![](https://github.com/roberrera/NeighborhoodCats/blob/master/NeighborhoodCats/Screenshots/device-2016-04-04-220600(reduced).png)
