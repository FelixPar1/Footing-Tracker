This code is a jogging tracker for Android.



The application opens with a main screen that allows you to access the map screen to record a race and to access the history screen.
When you start a run, the application tracks your location as you go, and displays the route you've taken on the map. Once the run is over, you can view the distance covered, time and average speed.
You can also check your current speed at any time during the race.
Finally, a save button allows you to save the race in the history.



The main code can be found in  /app/src/main/java/com/example/footingtrainmap
There are 3 activities: MainActivity, RaceActivity and HistoryActivity.
LocalisationService.java allows the application to run in the background and continue plotting the route when the phone is idle
Trip.java contains the informations of a Race (duration, distance, speed and all the points associated with the time). 
