# Getting Started

### Reference Documentation
This is an app to reflex and practice basic knowledge of reactive webflux.

For this sample app you just need the get an instance of mongoDB, so we can download it from the official site or use a docker image.
In case of you choose a docker image I leave below the file for docker compose I used during the development time of this app.
    
    services:
        mongodb:
            image: mongo
            ports:
                - '27017:27017'
            environment:
                MONGO_INITDB_ROOT_USERNAME: myuser
                MONGO_INITDB_ROOT_PASSWORD: mypassword

Then, once you have installed docker compose run the next command:

    docker-compose up

The command described above will pull, create and run the container image automatically.

Finally, you can just hit the run green button and execute the app.