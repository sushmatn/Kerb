# Kerb

**Kerb** - The app that takes over the notepad companies use for managing customer wait lists and transforms it into a self-served application that provides critical analytics for customer flow management. The customers can add themselves to the wait list when in proximity of the location with out crowding the entrance, they can see current wait time and plan accordingly. No need for buzzers, drop-in phone numbers and paper scribbles, our app allows customers to check-in hassle free and businesses to analyze critical customer inflow data. No waiting in line to get on the list, no more Kurb sitting, free data analytics for companies. The company can send a notification when the service is ready, call or text the customer all from within the app.
Based on current data the app estimates current average wait time and printed next to the company name in a yelp-like lister.
For this project we will use restaurants as company to demo the application. 

## User Stories

The following constitute the **required** functionality:
* [x]  Customer app - The app should list all the restaurants that are close to user's location. Each entry includes the wait time as well as the distance from user's location.
* [x]  Customer app - Clicking on a restaurant entry opens the details page and  displays the average wait time.
* [x]  Customer app - The 'Join wait list' button is grayed out if the user is outside of a 0.1 mile radius from the restaurant, otherwise it will become enabled.
* [x]  Customer app - Clicking on the 'Join wait list' button will display a dialog where the user will enter his/her name and number of people in the party. This adds an entry to the wait list and brings the user to the 'Waiting' page. This page displays the user's position in the wait list (# of parties that are on the wait list ahead of user). Also displays the estimated wait time.
* [x]  Restaurant App home page displays the restaurant name and provides a 'Open the wait list' button. Clicking on the button displays an empty list.
* [x]  Restaurant app - an entry is added to the list whenever a user joins the wait list.
* [x]  Restaurant app - has a menu drawer is available for creating new lists and other relevant actions.
* [x]  Restaurant app - can take following actions for an item on wait list: Seat, Remove, Call, Text, Notify, Edit party and wait time
    * [x]  Restaurant can 'Remove from list'. This removes user from wait list.
    * [x]  'User seated' also removes the user from the list.
    * [x]  'Table Ready' button sends a notification to the user that their table is ready

The following are **optional** features:
* [x]  Customer app - The app provides a map view that lists the restaurants around the user's current location and displays the wait time in a pin.
* [x]  Customer app - User can search for a restaurant by clicking on the search button in the actionbar.
* [x]  Customer app - The user can click on the favorite button to add the restaurant to their favorites.
* [ ]	 Customer app - The user should be allowed to filter the results by cuisine, distance ...? The filter options are saved using Shared Preferences.
* [x]  Customer app - User can click on the 'Leave wait list' button to quit waiting.
* [ ]  Customer app - For restaurants that do not use the wait list app, the app displays 'Report waittime' button instead of 'Join wait list'. Clicking on the button displays a dialog where the user can enter the estimate wait time provided by the hostess.
* [ ]  Restaurant app - Restaurant can see history data for current wait list with color coded status for each entry. History can be shown or hidden as part of the current view.
* [x]  Restaurant app - The restaurant needs to login using their unique username/password. Once logged in the username is recognized as the restaurant and should display the 'home' page. 
* [ ]  Restaurant app - Customers can also enter the list as a walk-in which is added manually by the host.A flag will indicate how the customer entered the list. If a walk-in, some of the actions will be disabled. 
* [ ]  Customer & Restaurant app app - Remove from list' removes the user from active list and sends to entry to history. 
* [ ]  Restaurant app - The app sends a notification after the estimated wait time to check if the user is still waiting or seated -> This data is used to determine the wait time.

The following are **bonus** features:
* [ ]  Customer app - When the user leaves 0.5 mile radius, he is prompted if he wants to stay on the list.
* [x]  Customer app - Details page provides a button to view restaurant reviews, photos, hours etc.
* [ ]  Customer app - a feedback mechanism like 'quick rating' which allows customers to provide feedback on the initial estimated wait time accuracy. When available, average wait time rating is displayed in the restaurant details screen.
* [ ]  Restaurant app - Restaurant wait list can be archived or deleted by the restaurant. An archived wait list saves data on server and it's a data source for canned analytics
* [ ] Restaurant app - has a Settings menu & View archived lists
* [ ] Restaurant app - can access pre-canned reports oferring analytical data on customer inflow. e.g: charts for daily/monthly seated vs dropped customers, average waittime slope, etc.
* [ ] Restaurant app - can see customer inflow forecast analytics
* [ ] Restaurant app - has settings/preferences such as max party size, check-in proximity, automatic drop-off after X occurs, custom logo, background, interface language

NOTE:
User can only join one wait list at any time.
Even if the app restarts, it detects the user's last status and if she was in the wait list takes her directly to the wait list page, same goes for the restaurant.

##Wireframes
Please find the wireframes in our dropbox folder by clicking [Here] (https://www.dropbox.com/sh/8gunrbatjv4eisk/AABTSGFCYovnmd0vueLFl9gea?dl=0) 

## Video Walkthrough 

Here's a walkthrough of implemented user stories:

- Restaurant Home page displays the restaurant name and provides 'Open wait list' button. Clicking on the button displays an empty list, an entry is added to the list whenever a user joins the wait list. The customer app allows you to browse the restaurants around your location, view the wait time, pictures, address, reviews, etc. 

<img src='https://github.com/sushmatn/Kerb/blob/master/gifs/Kerb_RestaurantApp_story1.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />


 <img src='https://github.com/sushmatn/Kerb/blob/master/gifs/Kerb_RestaurantApp_story2.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />
 
 - Detail video for customer app: https://github.com/sushmatn/Kerb/blob/master/gifs/demoday/mobizen_20151106_195415.mp4

GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Notes

Describe any challenges encountered while building the app.

## Open-source libraries used
<List open source libs here>

## License

    Copyright [2015] [name of copyright owner]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
