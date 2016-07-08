#Ferro

Simple and powerful MVP library for Android 

Ferro elegantly solves two age-old problems of Android Framework related to configuration changes:
* restore screen's data
* managing background tasks

First problem solved using permanent presenter, second - using freezing rx events (Observable doesn't unsubscribe).
Also new feature added - freeze rx event when screen becomes invisible, and defreeze it when screen goes to foreground.

The schematic work of ferro:

![SchematicImage](ferro.gif)

Ferro is divided into 3 layers, each of which adds behavior to the previous. So you can use only part of ferro.

The first layer:
##ferro-core
This library contains base classes for Activity  and Fragment (`PSSActivity, PSSFragmentV4`, PSS - persistent screen scope).

