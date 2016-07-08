#Ferro

Simple and powerful MVP library for Android 

Ferro elegantly solves two age-old problems of Android Framework related to configuration changes:
* restore screen's data
* managing background tasks

First problem solved using permanent presenter, second - using freezing rx event.
Also new feature added - freeze rx event when screen becomes invisible, and defreeze it when screen goes to foreground.

The schematic work of ferro:

![SchematicImage](ferro.gif)
