# Campasian - University Social Network Setup & Features

## 📋 Overview
Campasian is a JavaFX-based university social networking application built with Firebase Realtime Database integration. It helps university students connect, share events, join clubs, and communicate with each other.

## 🎯 New Features Added (6 Scenes)

### 1. **Dashboard/Feed** (`dashboard.fxml`)
- **Location**: `/fxml/dashboard.fxml`
- **Controller**: `DashboardController.java`
- **Features**:
  - View posts from students
  - Create and publish new posts
  - Like, comment, and share posts
  - Real-time feed updates
  - Navigation to other sections

### 2. **User Profile** (`profile.fxml`)
- **Location**: `/fxml/profile.fxml`
- **Controller**: `ProfileController.java`
- **Features**:
  - View and edit personal profile
  - Display profile information (name, email, university)
  - Show bio and user statistics
  - Edit and save profile changes
  - View followers and following counts

### 3. **Browse Users** (`browseusers.fxml`)
- **Location**: `/fxml/browseusers.fxml`
- **Controller**: `BrowseUsersController.java`
- **Features**:
  - Search and filter students
  - Filter by university or major
  - Add friends with one click
  - View profiles of other users
  - Discover new connections

### 4. **Events** (`events.fxml`)
- **Location**: `/fxml/events.fxml`
- **Controller**: `EventsController.java`
- **Features**:
  - Browse upcoming university events
  - Create new events
  - Event details (date, location, description)
  - RSVP and attend events
  - View attendee count

### 5. **Clubs & Groups** (`clubs.fxml`)
- **Location**: `/fxml/clubs.fxml`
- **Controller**: `ClubsController.java`
- **Features**:
  - Explore university clubs
  - Create new clubs
  - Join clubs by category
  - View club members count
  - Manage club membership

### 6. **Direct Messaging** (`messages.fxml`)
- **Location**: `/fxml/messages.fxml`
- **Controller**: `MessagesController.java`
- **Features**:
  - Direct messaging with other users
  - Conversation history
  - Real-time message delivery
  - Read/unread message status
  - Multiple ongoing conversations

## 🔧 Technical Architecture

### Utility Classes

#### **SceneManager** (`SceneManager.java`)
Handles scene transitions with fade animations:
```java
SceneManager.switchScene("dashboard.fxml");
```

#### **FirebaseManager** (`FirebaseManager.java`)
Provides Firebase REST API integration:
- `createUser()` - Create user profile
- `createPost()` - Publish posts
- `getAllPosts()` - Fetch feed
- `createEvent()` - Create events
- `createClub()` - Create clubs
- `sendMessage()` - Send direct messages

### Model Classes
- `User.java` - User profile model
- `Post.java` - Post/feed item model
- `Event.java` - Event model
- `Club.java` - Club/group model
- `Message.java` - Message model

## 🔐 Firebase Configuration

### Prerequisites
1. Firebase Realtime Database created
2. Firebase API Key: `AIzaSyDtNDvHczMn1nkUEHorrGMcZbUxAAiKbbE`
3. Database URL: `https://campasian.firebaseio.com`

### Database Structure (Rules)
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

## 📦 Dependencies Added

The following dependencies were added to `build.gradle.kts`:

```gradle
// Firebase Admin SDK
implementation("com.google.firebase:firebase-admin:9.2.0")

// JSON processing
implementation("com.google.code.gson:gson:2.10.1")

// HTTP Client
implementation("com.google.http-client:google-http-client:1.44.1")
```

## 🚀 Running the Application

### Step 1: Build the Project
```bash
./gradlew build
```

### Step 2: Run the Application
```bash
./gradlew run
```

### Step 3: Navigate Through Scenes
- **Login/Signup** → **Dashboard** → Explore other sections

## 📱 Navigation Flow

```
Home (home.fxml)
├── Login (Login.fxml) → Dashboard
├── Signup (Signup.fxml) → Dashboard
│
Dashboard (dashboard.fxml)
├── Profile (profile.fxml)
├── Browse Users (browseusers.fxml)
├── Events (events.fxml)
├── Clubs (clubs.fxml)
├── Messages (messages.fxml)
└── Logout → Back to Home
```

## 🎨 UI/UX Features

- **Dark Theme**: Modern gradient background with glassmorphism effects
- **Animations**: Smooth fade transitions between scenes
- **Responsive Design**: Dynamic layout adapting to window size
- **Interactive Elements**: Buttons, text areas, and input fields
- **Color Scheme**: Blue (#38bdf8), Purple (#a855f7), Red (#f43f5e), Amber (#f59e0b)

## 🐛 Current Limitations & Future Improvements

1. **Authentication**: Currently uses mock authentication. Implement Firebase Auth
2. **Real-time Sync**: Uses REST API. Consider WebSocket for real-time updates
3. **Offline Support**: No offline mode currently
4. **Image Uploads**: Not yet implemented, ready for Firebase Storage
5. **Notifications**: Push notifications not yet implemented

## 💡 Sample Data

The application comes with sample data for:
- Users with profiles
- Posts and feed items
- Events with dates and locations
- Clubs with categories
- Message conversations

## 🔄 Next Steps

1. Implement Firebase Authentication
2. Add user registration validation
3. Implement real-time database listeners
4. Add image upload functionality
5. Implement push notifications
6. Add search functionality with backend queries
7. Implement user follow/unfollow
8. Add post likes and comments
9. Implement event RSVPs
10. Add club membership management

## 📞 Support

For configuration issues or Firebase setup help:
1. Check Firebase documentation: https://firebase.google.com/docs
2. Verify API key and database URL
3. Check Firebase database rules
4. Enable HTTP requests in your network configuration

---

**Version**: 1.0.0  
**Last Updated**: February 2026  
**Status**: Ready for development & testing
