## Hour Tracking Service

This service is responsible for tracking hours worked on projects.

### Features

- Manual hour tracking
- Syncing with third-party productivity tools (currently only Clockify)
- Exporting hours to CSV/JSON
- Deleting hours for a specific project
- Updating hours for a specific project
- Getting hours for a specific project

### Automatic Syncing

The hour tracking service can automatically sync hours from Clockify.

#### Setup

Add your Clockify API key to the database:

```bash
docker exec -i mongodb mongosh hourtrackingdb --eval 'db.thirdPartyAccounts.insertOne({
  _id: Binary.createFromBase64("1EGb4gCEDlUAAERVZkQWpw==", 3),
  userId: Binary.createFromBase64("1EGb4gCEDlUAAERVZkQWpw==", 3),
  provider: "clockify",
  authToken: "YOUR_CLOCKIFY_API_KEY",
  connectedDate: new Date(),
  active: true,
  _class: "com.group8.hourtracking.model.ThirdPartyAccount"
})'
```

You have to get your API key from [Clockify Profile Settings](https://clockify.me/user/settings) (make an account first).

#### Sync Hours

```bash
curl -X POST "http://localhost:8082/hours/sync/clockify?userId=550e8400-e29b-41d4-a716-446655440000"
```

This creates projects automatically (if not existing) and syncs all your Clockify time entries.
