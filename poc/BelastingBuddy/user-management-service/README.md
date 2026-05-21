# User Management Service

This service handles **user registration, login, and authentication token management**.  
It acts as a secure proxy between the frontend and **Keycloak**.
It is the only service that actually sees the user credentials and it acts as a private client so it can access the **Keycloak** admin endpoints.

## Registration

Create a new user account.
Posts the new user credentials, if there is no conflict with the username or email the user is created and it returns the UUID.
#### Endpoint

` POST /api/public/register`

#### Request Body (JSON)

```json
{
  "email": "IAmFromWonderLand@napTime.com",
  "username": "alice8",
  "password": "Password",
  "firstName": "Alice",
  "lastName": "FromWonderland"
}
```

#### Expected Response

```json
{
  "id": "e594d1fd-9cce-4a17-95ff-54d5b87f3249",
  "status": "CREATED"
}
```

## Login

Obtain a pair of JWT tokens to authenticate future requests.
Post the username and password, and receive the access toke, refresh token, as well as metadata about them and the session.
#### Endpoint

` POST /api/public/login`

#### Request Body (JSON)

```json
{
  "username": "alice8",
  "password": "Password"
}
```

#### Expected Response

```json
{
  "access_token": "<JWT_ACCESS_TOKEN>",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "<JWT_REFRESH_TOKEN>",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "26693175-9fd5-48b3-8237-d3b6123b8aab",
  "scope": "profile email"
}
```