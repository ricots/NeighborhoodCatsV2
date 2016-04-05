# ![](https://ga-dash.s3.amazonaws.com/production/assets/logo-9f88ae6c9c3871690e33280fcf557f33.png) Neighborhood Cats (Android) Readme :cat:

Neighborhood Cats is a photo-sharing app for Android that lets you keep your cat sightings organized and accessible, while retaining the ability to show the world the adorable (or hideous) kitty you've encountered.

In the neighborhood my wife and I used to live in we would pass stray cats as well as domesticated cats sitting in windows. We would play a game in which we would count how many cats we spotted each day, and I would take photos. But I'd often forget on which block I spotted each cat, and the photos would get buried in my gallery as I took other (non-cat) photos. After we moved to a new neighborhood, I set out to build an app that records these cat sightings and allows you to better remember your feline friends and where you saw them.

Bugs:
 - On Lollipop or KitKat: Saving an item via the camera crashes the app.
 - On Lollipop or KitKat: Saved items created via the device's photo gallery do not appear in the list.
 - If the image is an older image being selected via Google Photos, the image will not be loaded because it has to be downloaded first (so there is no image file path to load when the app is looking for the file path).
 - Search does not return filtered results.
