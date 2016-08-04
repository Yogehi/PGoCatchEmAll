# PGoCatchEmAll
# NOTE: THIS TOOL DOES NOT WORK AS OF AUGUST 3, 2016!

XPosed module that until August 3, 2016, guaranteed all versions of Pokemon Go to always receive the "Excellent" and "Curve Ball" bonus EXP as long as you hit the Pokemon. This module hooks onto the "doSyncRequest" method in "Pokemon Go" to read all data the game was about to send and then modifying that data. I'm publishing this tool for archival purposes.

Note that while this module focuses on altering a user’s throw attempt, the “doSyncRequest” is where all requests made by “Pokemon Go” are gathered before being sent to the servers. With the correct adjustments, this module could do much more.

### Description on how Pokemon Go communicates via serialized data
Pokemon Go communicates with the web server https://pgorelease.nianticlabs.com. The game uses Google’s Protobuf (https://github.com/google/protobuf) format to serialize the data when the client and server are sending/receiving data. A sample request which involves trying to catch a Nidoran♀ would look something like this:

<image1>

Putting this data through both a hex editor and a protobuf plugin available for Bupsuite would show the following:

<image2>
<image3>

Thanks to POGOProtos (https://github.com/AeonLucid/POGOProtos) we can start to decode information about this specific request. Since “PGo Catch Em All” focuses on altering the catch attempts by the player and the example request is attempting to catch a Nidoran♀, we will only decode that portion of this request.
Lets break this down!

<image4>

Changes are as followed:

* Orange = how many bytes in hexadecimal will the next set of data will be. 30 in hexadecimal is 48 so the next set of data will be 48 bytes long. This is used to indicate a new “bracket”.

* Red = the request type in hexadecimal. 67 in hexadecimal is 103, so the request type is 103, or if you reference the data at https://github.com/AeonLucid/POGOProtos/blob/master/src/POGOProtos/Networking/Requests/RequestType.proto you will see that 103 is also called “CATCH_POKEMON”. This is the request type that is sent every time a user throws a Pokeball, whether or not the user misses or hits the Pokemon.

* Dark Green = how many bytes in hexadecimal will the next set of data will be. 2C in hexadecimal is 44 so the next set of data will be 44 bytes long. This is used to indicate a new “bracket”.

* Pink = the “encounter ID” which is set by the server whenever the client receives information about nearby Pokemon.

* Grey = the Pokeball type thrown in hexadecimal format.
  * 1 = Pokeball
  * 2 = Greatball
  * 3 = Ultraball

* Brown = the reticle size when the Pokeball hit the Pokemon. This value is sent in IEEE754 Doubles format and is sent in reverse order in hexadecimal format.
  * 0x3ff9053400000000 = 1.56
  * In hexadecimal, this value would be shown as “00 00 00 00 34 05 F9 3F”
  * In all versions of “Pokemon Go” before 0.31, this varied usually between 1.0 and 1.99. The smaller the reticle, the closer to 1.99 this value would become.
  * In version 0.31, this value now varies between 0.1 and 0.99. More on this later.
* Yellow = spawn point, sent in ASCII format.

* Light green = indicates if the Pokeball hit the Pokemon. This set of data is not sent to the server if the user misses the Pokemon.

* Blue = hit position, or if the Pokeball hit within the reticle when throwing the ball. This value is sent in IEEE754 Doubles format and is sent in reverse order in hexadecimal format. This set of data is not sent to the server if the user does not hit within the reticle.
  * 0x3ff0000000000000 = 1.0 – this value will always be sent no matter where within the circle the Pokeball hit. So think of this as a true/false statement.
  * In hexadecimal, this value would be shown as “00 00 00 00 00 00 F0 3F”

An addition set of data can also be sent to the server, and that is a “spin modifier” value. Depending on if the Pokeball was thrown in a curveball type of way, this value would be also sent as an IEEE754 Doubles format.

### Bonus Experience Points
In Pokemon Go before version 0.31, the user was awarded a bonus in experience points based on a grading system. The user could either receive a grade of “Nice”, “Great” or “Excellent” based on how small the reticle size was and if the user’s Pokeball hit inside of the reticle. If the Pokemon was successfully captured with one of these grades, the user would receive 10 bonus experience points for a “Nice” grade, 50 experience points for a “Great” grade, and 100 experience points for an “Excellent” grade.

The grade was determined by what size the reticle was when the Pokeball hit the Pokemon and if the Pokeball hit inside of the reticle.
Another bonus of 10 experience points is given if the Pokeball curves during the throw. This is called a “curve ball bonus” and is still present in version 0.31 of Pokemon Go.

If we wanted to throw a ball that would yield an “Excellent” throw as well as the “Curve Ball” bonus, the request would look something like this:

<image 5>

Changes are as followed:

*  Orange = the size of the next set of data has been increased since data about the “Spin Modifier” has been added. The size increased from 48 bytes (30 in hexadecimal) to 57 bytes (39 in hexadecimal).

*  Green = the size of the next set of data has been increased since data about the “Spin Modifier” has been added. The size increased from 44 bytes (2C in hexadecimal) to 53 bytes (35 in hexadecimal).

*  Brown = the reticle size value was adjusted from 1.56 to 1.95 which is considered to be a super small reticle since the value is so close to 2.0.

*  Purple = “spin modifier” data that was added to the request. This value is sent in IEEE754 Doubles format and is sent in reverse order in hexadecimal format.
  *	0x3feb333333333333 = 0.85 which is considered to be a really curvy throw
  *	In hexadecimal, this value would be shown as “33 33 33 33 33 33 EB 3F”

In order to receive the “Excellent” experience point bonus, both the reticle size must be small (in this case, the size is 1.95) and the hit position must be true (it is still true from the first example). Because the request has met both of those requirements, the user would receive an “Excellent” experience point bonus. If the user did not hit within the reticle, then no information would be sent regarding the “hit position” and therefore the user would not receive this bonus.

The user also sent data to the server indicating that the ball curved at a good angle as it was thrown. Therefore the server would also give the “Curve Ball” experience point bonus.

### How PGo Catch Em All Works

In order to sync with the server, the client has a method called “doSyncRequest” located in the Java class “com.nianticlabs.nia.network.NiaNet”. This method is used to send data to the server.

The following data is sent to this method:

*	Object
*	Request ID
*	URL (where this request will be sent to)
*	Method
*	Headers
*	ByteBuffer (the serialized data)
*	ByteBuffer Offset (where the serialized data starts within the ByteBuffer)
*	ByteBUffer Size (the size of the ByteBuffer)

PGo Catch Em All reads every ByteBuffer before “doSyncRequest” sends the data to the server. As each ByteBuffer is read, this module determines the following:

*	Is the request type a “CATCH_POKEMON” request type?
*	Did the user hit the Pokemon?

If both requirements are met, then this module will break down the request into the following categories:

*	Size of the throw data in both brackets
*	Encounter ID
*	Pokeball type used
*	Reticle size
*	Spawn location
*	Hit Pokemon value
*	Spin modifier (if present)
*	Hit position (if present)

Next, this module will add/alter the following data:

*	Change the “reticle size” value to 1.95
*	Change or add a “spin modifier” value of 0.85
*	If missing, will add a “hit position” value of 1.0
*	Adjust both data size values to match the new size if any data was added (spin modifier value and hit position value)

After reassembling the new data, this module will adjust the “ByteBuffer” size value to match the new size of the serialized data.

## What's with version 0.31 and my catch rate!?

In version 0.31 of Pokemon Go, it was reported by users that the experience point bonus from getting a “Nice”, “Great” or “Excellent” throw was taken out. After further investigation, it was determined that version 0.31 of Pokemon Go sends new values based on reticle size. This new value has been observed to be between 0.1 and 0.99. 

At the time of this writing, the server is still configured to accept reticle size values greater than 1.0 and gives out experience point values based on reticle size values from Pokemon Go versions prior to 0.31. Because of this, it is impossible for a user running version 0.31 of Pokemon Go to achieve reticle values that the server would normally give an experience point bonus for. 
