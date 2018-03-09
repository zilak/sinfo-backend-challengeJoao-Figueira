# sinfo-backend-challengeJoao-Figueira

### How to build and run application:

1- Open Intellij and select "File ->New->Project from version control->GitHub"

2- In "Git Repository URL" paste the link of this repository.

3- Clone!

4- Build the project in Intellij

5- Run Main.class

6- In [Postman](https://www.getpostman.com/) (Application to test the API ) type the URLs and headers needed to run the application. 






### API Documentation 

#### localhost:8000/dealers       
###### method: GET

Returns in JSON a list of all vehicles by dealers.

#### localhost:8000/model  
###### method: GET

Retunrs in JSON a list of all vehicles by model.

### localhost:8000/transmission 
###### method: GET

Retunrs in JSON a list of all vehicles by transmission.

### localhost:8000/fuel 
###### method: GET

Retunrs in JSON a list of all vehicles by fuel.


### localhost:8000/finddealer 
###### method: GET
###### headers: 
| Key        | Value(Example)           | 
| ------------- |:-------------:|
| lat     | 38.1044 | 
|long      | -9.10      |  



Returns in JSON the closest dealer.


### localhost:8000/bookings 
###### method: GET

Returns in JSON all bookings.

### localhost:8000/bookings 
###### method: POST
###### headers:
| Key        | Value(Example)           | 
| ------------- |:-------------:|
| id     | 846f2d9b-be82-47ac-88dc-3bb20581d777 | 
|vehicleId      | 136fbb51-8a06-42fd-b839-c01ab87e2c6c      |  
| firstName | Joao     |    
|lastName | Figueira| 
|pickupDate| 2018-03-20T10:30:00|
|createdAt| 2018-02-27T08:42:46.296|

Creates a new booking.


### localhost:8000/bookings 
###### method: DELETE
###### headers:
| Key        | Value(Example)           | 
| ------------- |:-------------:|
| id     | 846f2d9b-be82-47ac-88dc-3bb20581d777 | 
|vehicleId      | 2018-02-29T08:42:46.291     |  
| cancelledReason | Weather     |    


Cancel a booking.



### Explanation about my choices

*The source code is in Jave because is the languague that I am more familiar.

*The Server is multithreads so it handles multiple request at the same time.

*It is possible to do a booking of a one with the same characteristics as long it has been cancelled before the "createdAt" date

*To prevent data races and data omission I implement a lock system to create/delete/see  bookings.
