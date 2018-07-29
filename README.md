# StackOverflow tag analizer

App that's calculate tag and answered question. App is a REST API with 1 end point
> /search
you can set parameter of query string tag to search need parameter

EXAMPLE:

> /search?tag=clojure&tag=scala"

All tag input validate and except string in ru or eng with number (whitespace etc not supported)

REQUEST EXAMPLE : 
>  curl -X GET http://localhost:5555/search?tag=scala 

Response generate in json with pretty printing

## Usage

Server start command:

> lein run

By default they start in 0.0.0.0 port 5555 with 10 connection

Server use config in:

> resources/config.edn