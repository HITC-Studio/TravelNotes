# TravelNotes
*Android Mobile App; Google Maps with text saving abiltiy per marker. Abiltiy to also link images to markers as well. Capable of creating multiple favorite lists.*
## Most Code Stolen From
### For the image gallery and image swiping
https://www.loopwiki.com/application/create-gallery-android-application/
### For the slide up bottom layout with all the buttons
https://codingmountain.com/google-map-android-application-ui-clone-using-bottomsheet-layout/
## API Level
This code works on Android, with API >= 19. 
## API Keys
You will need API keys for the following services: 
- Places API
- Maps SDK for Android
- Places SDK for Android
## How to use
### Current Location
The App shows a simple street view map, with a large blue dot as the user's current location. The user can also press the blue cross-hair button to move the map to their current location.
### Search Boxes
The user has 2 text boxes at the top of the screen. 
- Top box uses Google's Places SDK to search for matching addresses based on their input
- Second box allows the user to search for general/generic terms (e.g. food, bars, gas stations, etc...)
### Markers
- The user can do a long-press on the map to create their own marker.
- Selecting a marker on the map will have it current name appear above it
### Bottom Drawer (Slide Up Screen)
Tapping or sliding the bottom drawer upwards provides the additional options:
1. Drop Down Box: Select Favorite List
   - A drop down box to select a favorite list, that will populate the map with the saved markers.
     - Or None (if chosen)
     - Or All Markers (from all lists, if chosen)
2. Button: Create List
   - Create a new list
3. Button: Delete List
   - Delete a list
4. Button: Save Marker
   - Save the selected Marker into the selected Favorite List
     - Can not be currently None or All Markers
5. Button: Remove Marker
   - Remove the selected Marker from the selected Favorite List
     - Does NOT remove the marker from the map
6. Button: Clear Markers
   - Clears any non-saved markers from the map
7. Text Box: Name
   - Allow the user to re-name the selected marker
8. Text Box: Notes 
   - A blank text box to allow the user to write any notes they want about the selected marker.
9. Button: Camera
   - Opens up the users phone storage, where they can select 1 or more images to link to the selected marker (these images are not copied, only linked).
