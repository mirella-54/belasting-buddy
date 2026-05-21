 
## Document storage service 

This service allows to store documents permanently. It uses Firebase storage to store files and Google Key Management Service (KMS) to encrypt file contents. It is best to test it's functionality with other services running. To do that run: ```docker compose up``` in /poc/BelastingBuddy.

 The service also offers 'upload-locally' function, which allows to store documents locally. It is the best for testing kafka or implementations not directly related to documents storage, but requiring it as a step. 

To run the microservice locally:
1. In config/SecurityConfig default value for the .env variables needs to be provided. <i>APP_KEYCLOAK_USERS_JWKS</i> and <i>APP_KEYCLOAK_SERVICES_JWKS</i>values need to be added.  
2. Similarly in the ServiceTokenProvider. <i>APP_KEYCLOAK_SERVICES_TOKEN_URL</i>,<i> APP_KEYCLOAK_SERVICES_CLIENT_ID</i> and <i>APP_KEYCLOAK_SERVICES_CLIENT_SECRET</i> values need to be added.

Then run this command: 

```curl
./mvnw -pl document-storage-service spring-boot:run
```

Register a user:
``` curl 
curl -X POST http://localhost:8081/api/public/register \
  -H "Content-Type: application/json" \
  -d '{                                              
        "email": "nikosev111@fogdiver.com",
        "username": "alice8",
        "password": "Password",
        "firstName": "lala",
        "lastName": "blabla"
      }'

```
Login a user: 

```curl

curl -X POST http://localhost:8081/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice8", "password": "Password"}'

```

Make a upload request to firebase:

```curl

curl -v -X POST \
  -H "Authorization: Bearer SESSION KEY FROM LOGIN" \
  -F "file=@FILEPATH" \
  -F "content=Some text content" \
  -F "userID=12345" \  "http://localhost:8083/api/files-archive/upload"

```

Make a upload request to store file locally:

```curl

curl -v -X POST \
  -H "Authorization: Bearer SESSION KEY FROM LOGIN" \
  -F "file=@FILEPATH" \
  -F "content=Some text content" \
  -F "userID=12345" \  "http://localhost:8083/api/files-archive/upload-local"

```


Make a 'getFile' request (gets file content):

```curl

curl -X POST http://localhost:8083/api/files-archive/getFile \
     -H "Authorization: Bearer <SESSION TOKEN> \
     -F "fileName=get-pip.py" \
     -F "userID=12345"

```

Make a download request: 

```curl

curl -X POST http://localhost:8083/api/files-archive/download \
     -H "Authorization: Bearer <SESSION TOKEN> \
     -F "fileName=get-pip.py" \
     -F "userID=12345"

```

Make a download zip request (allows to download files for a specific time period):

```curl

curl -X POST http://localhost:8083/api/files-archive/download-zip \
     -H "Authorization: Bearer <SESSION KEY>" \
     -F "startDate=3-Oct-2025" \
     -F "endDate=25-Oct

```

Make a delete file request:

``` curl

curl -X POST http://localhost:8083/api/files-archive/delete \
     -H "Authorization: Bearer <SESSION KEY>" \
     -F "fileName=get-pip.py" \
     -F "userID=12345"

```

There is another request implemented which was left just for testing called hello. To make that request please do: 
``` curl

curl -X POST http://localhost:8083/api/files-archive/hello \
     -H "Authorization: Bearer <SESSION KEY>" \

```
The response will be "Hello World", which suggests the service is up and running. Of course "userID" and "fileName" fields above are just an example.
