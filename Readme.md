# Test Purpose App with local database and API calls

## Functionality
The app is able to save to a local sqlite database and serialize the data contained to then send it to an arbitrary API specified by the user.
The functionality is supposed to stay very basic and simple, in order to be reverse engineered and understood by students taking the challenge.
It will be run virtually or delivered as APK and possibly be prepopulated with user data

Into the workings of the API call, a vulnerability will be inserted in order to be exploited by the students.

-   click on the symbol to the right to add a new, potentially riskful person.
-   the symbol to the right opens the API call activity (via POST-Request)
-   all logic lies within the kt files in folder /app/src/main/java/com/example/db