# Firebase Integration Guide for Campasian

## 🔌 Firebase Configuration Setup

### Step 1: Enable Firebase Realtime Database

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your "campasian" project
3. Navigate to **Realtime Database**
4. Click **Create Database**
5. Choose **Start in Test Mode** (for development)
6. Select region: **us-central1** (or closest to you)

### Step 2: Set Firebase Database Rules

In the **Rules** tab, replace the existing rules with:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

**⚠️ Warning**: This is for development only. For production, use:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": true,
        ".write": "auth.uid == $uid"
      }
    },
    "posts": {
      ".read": true,
      ".write": "auth != null"
    },
    "events": {
      ".read": true,
      ".write": "auth != null"
    },
    "clubs": {
      ".read": true,
      ".write": "auth != null"
    },
    "messages": {
      "$from": {
        "$to": {
          ".read": "auth.uid == $from || auth.uid == $to",
          ".write": "auth.uid == $from"
        }
      }
    }
  }
}
```

### Step 3: Verify Configuration in Code

The Firebase Manager uses these credentials (already configured):
- **API Key**: `AIzaSyDtNDvHczMn1nkUEHorrGMcZbUxAAiKbbE`
- **Database URL**: `https://campasian.firebaseio.com`
- **Project ID**: `campasian`

Location: `src/main/java/com/abir/demo/utils/FirebaseManager.java`

## 📊 Database Structure

The application creates the following JSON structure:

```
campasian/
├── users/
│   └── {userId}/
│       ├── email: string
│       ├── fullName: string
│       ├── university: string
│       ├── bio: string
│       ├── profileImage: string
│       ├── createdAt: timestamp
│       ├── followers: number
│       └── following: number
│
├── posts/
│   └── {postId}/
│       ├── userId: string
│       ├── content: string
│       ├── imageUrl: string
│       ├── timestamp: timestamp
│       ├── likes: number
│       └── comments: number
│
├── events/
│   └── {eventId}/
│       ├── name: string
│       ├── description: string
│       ├── dateTime: string
│       ├── location: string
│       ├── createdAt: timestamp
│       └── attendees: number
│
├── clubs/
│   └── {clubId}/
│       ├── name: string
│       ├── description: string
│       ├── category: string
│       ├── createdAt: timestamp
│       ├── members: number
│       └── image: string
│
└── messages/
    └── {fromUserId}_{toUserId}/
        └── {messageId}/
            ├── from: string
            ├── to: string
            ├── text: string
            ├── timestamp: timestamp
            └── read: boolean
```

## 🔧 Using FirebaseManager

### Creating a User
```java
FirebaseManager.createUser("user123", "email@uni.edu", "John Doe", "State University");
```

### Creating a Post
```java
FirebaseManager.createPost("user123", "Just finished my project!", "");
```

### Getting All Posts
```java
String posts = FirebaseManager.getAllPosts();
```

### Creating an Event
```java
FirebaseManager.createEvent("Tech Summit", "Annual tech conference", "2026-03-15", "Main Hall");
```

### Creating a Club
```java
FirebaseManager.createClub("Coding Club", "Learn programming", "Technology");
```

### Sending a Message
```java
FirebaseManager.sendMessage("user1", "user2", "Hey, how are you?");
```

## 🔄 Real-time Data Binding (Future Enhancement)

To implement real-time updates, extend FirebaseManager with:

```java
public static void listenToUserPosts(String userId, Consumer<List<Post>> callback) {
    String url = DATABASE_URL + "/posts.json?orderByChild=userId&equalTo=" + userId;
    // Implement WebSocket or polling mechanism
}
```

## ✅ Testing the Integration

### Test 1: Create User
```java
boolean success = FirebaseManager.createUser("test_user", "test@uni.edu", "Test User", "Test University");
System.out.println("User created: " + success);
```

### Test 2: Create Post
```java
boolean success = FirebaseManager.createPost("test_user", "Test post content", "");
System.out.println("Post created: " + success);
```

### Test 3: Send Message
```java
boolean success = FirebaseManager.sendMessage("user1", "user2", "Test message");
System.out.println("Message sent: " + success);
```

## 🚀 Deployment Checklist

- [ ] Enable Firebase Authentication (Email/Password)
- [ ] Update Firebase rules for production
- [ ] Implement auth state persistence
- [ ] Add input validation
- [ ] Implement error handling and logging
- [ ] Test all CRUD operations
- [ ] Add offline capabilities
- [ ] Implement data encryption
- [ ] Set up Firebase backups
- [ ] Monitor Firebase usage and quotas

## 📱 API Reference

### FirebaseManager Methods

#### `createUser(userId, email, fullName, university): boolean`
Creates a new user profile in the database.

#### `getUser(userId): JsonObject`
Retrieves a user's profile data.

#### `createPost(userId, content, imageUrl): boolean`
Creates a new post in the feed.

#### `getAllPosts(): String`
Retrieves all posts as JSON string.

#### `createEvent(eventName, description, dateTime, location): boolean`
Creates a new university event.

#### `getAllEvents(): String`
Retrieves all events.

#### `createClub(clubName, description, category): boolean`
Creates a new club or group.

#### `getAllClubs(): String`
Retrieves all clubs.

#### `sendMessage(fromUserId, toUserId, message): boolean`
Sends a direct message between two users.

## 🐛 Troubleshooting

### Issue: "Failed to PUT request"
**Solution**: Check if Firebase database URL and API key are correct.

### Issue: "Connection timeout"
**Solution**: Verify internet connection and Firebase service status.

### Issue: "Permission denied"
**Solution**: Update Firebase rules to allow write access for authenticated users.

### Issue: "JSON parsing error"
**Solution**: Ensure response format matches expected structure.

## 📚 Resources

- [Firebase Realtime Database Documentation](https://firebase.google.com/docs/database)
- [Firebase Console](https://console.firebase.google.com)
- [Firebase REST API](https://firebase.google.com/docs/database/rest/start)
- [Firebase Security Rules](https://firebase.google.com/docs/rules)

---

**Last Updated**: February 2026
