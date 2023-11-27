# Overview

This README will document all the changes that are part of my HW04. 

All affected files/folders:
 - MainActivity.kt
 - UserProfileActivity.kt
 - UserSetupActivity.kt
 - RequestFormActivity.kt
 - activity_user_setup.xml
 - activity_user_profile.xml

Accompanying PRs:
 - https://github.com/ucsb-cs184-f23/pj-android-02/pull/91
 - https://github.com/ucsb-cs184-f23/pj-android-02/pull/101

Explanation:

The MainActivity.kt file contains improvements to how users are stored in the database to avoid duplication/overwriting existing users and determine whether to route to UserSetup activity or UserSelection activity.
The UserProfileActivity.kt file contains changes to show users' personal info and allow them to change the info if needed.
The UserSetupActivity.kt file contains the code for the screen where users can input their personal info and code to add form validation
The RequestFormActivity.kt file contains changes to add form validation and to autofill the address field.
Both the xml files just contain layout changes that fit our app's branding.

Pictures:

<img width="234" alt="Screenshot 2023-11-27 at 2 34 13 PM" src="https://github.com/ucsb-cs184-f23/pj-android-02/assets/86935334/c73e03c8-16e0-4779-99ca-1793b3872d2e">
<img width="243" alt="Screenshot 2023-11-27 at 2 34 19 PM" src="https://github.com/ucsb-cs184-f23/pj-android-02/assets/86935334/c7b5c979-bd1a-40fe-bf09-5fe9c32085cc">
<img width="242" alt="Screenshot 2023-11-27 at 2 34 33 PM" src="https://github.com/ucsb-cs184-f23/pj-android-02/assets/86935334/09c61dde-1a38-4baa-8fe8-0085391a8170">
<img width="245" alt="Screenshot 2023-11-27 at 2 34 37 PM" src="https://github.com/ucsb-cs184-f23/pj-android-02/assets/86935334/be510a73-cb1c-4271-aed7-cef2afaec67d">
<img width="246" alt="Screenshot 2023-11-27 at 2 34 46 PM" src="https://github.com/ucsb-cs184-f23/pj-android-02/assets/86935334/d25ee485-28cd-4721-8fec-bf1468ea76d6">
<img width="240" alt="Screenshot 2023-11-27 at 2 34 51 PM" src="https://github.com/ucsb-cs184-f23/pj-android-02/assets/86935334/886088aa-8c36-4e76-9591-ccd2d28f90c6">
