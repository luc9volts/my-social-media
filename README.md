# New Social Media Practice.

Generates a list of new connection suggestions for a given profile, counting how many mutual friends a profile has with another profile.

## Installation

- Clojure 1.9.0
- Leiningen 2.8.1 on Java 1.8.0
- Midje 1.9.1
- Pedestal 0.5.4

## Usage

To execute the tests:

    $ lein midje
    
_All checks (20) succeeded_

To execute the application:

    $ lein run

## Features

###_"Be able to add a new profile" :_

    POST localhost:8080/profiles
    
With the parameters _name_ and _privacy (true or false)_.

The url for the new resource will be returned in response header _Location_ entry. 

###_"Be able to tag two profiles as connected" :_

    POST localhost:8080/profiles/:profile-id/friends
    
With the parameter _profile-friend-id_ of the connection.

###_"Be able to generate a list of new connection suggestions for a certain profile" :_

    GET localhost:8080/profiles/:profile-id/suggestions
    
Profiles with more mutual friends will appear at the top of the list and profiles that have opted for privacy will not appear in the list.

### Another features

Get profile:

    GET localhost:8080/profiles/:profile-id
    
Change profile details:

    PUT localhost:8080/profiles/:profile-id

With the parameters _name_ and _privacy (true or false)_.