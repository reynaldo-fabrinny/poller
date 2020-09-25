# KRY code assignment
Simple service poller 

The service consists of a backend service written in Vert.x (https://vertx.io/) 
that keeps a list of services (defined by a URL), and periodically does a HTTP GET to each and saves the response ("OK" or "FAIL").

Using: 
- Java 13
- vertx.io tookit
- Booststrap 4.1.3
- JQuery 3.5.1

Some of the issues are critical, and absolutely need to be fixed for this assignment to be considered complete.
There is also a Wishlist of features in two separate tracks 

Critical issues

- Whenever the server is restarted, any added services disappear
- There's no way to delete individual services
- We want to be able to name services and remember when they were added
- The HTTP poller is not implemented

Frontend/Web track:

- We want full create/update/delete functionality for services
- The results from the poller are not automatically shown to the user (you have to reload the page to see results)
- We want to have informative and nice looking animations on add/remove services

Backend track

- Simultaneous writes sometimes causes strange behavior
- Protect the poller from misbehaving services (for example answering really slowly)
- Service URL's are not validated in any way ("sdgf" is probably not a valid service)
- A user (with a different cookie/local storage) should not see the services added by another user
