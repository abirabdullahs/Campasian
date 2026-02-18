# Campasian - Quick Start Guide

## 🎯 What's Been Added

Your Campasian application now includes **6 new scenes** with complete Firebase database integration:

1. **Dashboard** - Main feed with posts
2. **Profile** - User profile management
3. **Browse Users** - Find and connect with other students
4. **Events** - Create and manage university events
5. **Clubs** - Join and manage student clubs
6. **Messages** - Direct messaging between users

## 🚀 Quick Start (3 Steps)

### Step 1: Build the Project
```bash
cd d:\camousConnect
./gradlew build
```

### Step 2: Run the Application
```bash
./gradlew run
```

### Step 3: Login
- Click "Get Started" on the home screen
- Enter any username and password
- You'll be redirected to the Dashboard

## 📱 Navigation from Dashboard

From the Dashboard, you can access:
- 👤 **Profile** - View and edit your profile
- 👥 **Browse** - Find and add other students
- 🎉 **Events** - Create and attend events
- 🎓 **Clubs** - Join clubs and groups
- 💬 **Messages** - Chat with other users

## 🔧 Firebase Setup (Important!)

### Enable Your Firebase Database
1. Go to: https://console.firebase.google.com
2. Select your "campasian" project
3. Go to **Realtime Database**
4. Click **Create Database**
5. Choose **Start in Test Mode**
6. Replace the Rules with:

```json
{
  ".read": true,
  ".write": true
}
```

## 📁 New Files Created

### Controllers (6 new)
- `src/main/java/com/abir/demo/controllers/DashboardController.java`
- `src/main/java/com/abir/demo/controllers/ProfileController.java`
- `src/main/java/com/abir/demo/controllers/BrowseUsersController.java`
- `src/main/java/com/abir/demo/controllers/EventsController.java`
- `src/main/java/com/abir/demo/controllers/ClubsController.java`
- `src/main/java/com/abir/demo/controllers/MessagesController.java`

### FXML Files (6 new)
- `src/main/resources/fxml/dashboard.fxml`
- `src/main/resources/fxml/profile.fxml`
- `src/main/resources/fxml/browseusers.fxml`
- `src/main/resources/fxml/events.fxml`
- `src/main/resources/fxml/clubs.fxml`
- `src/main/resources/fxml/messages.fxml`

### Utility Classes (2 new)
- `src/main/java/com/abir/demo/utils/SceneManager.java` - Scene navigation
- `src/main/java/com/abir/demo/utils/FirebaseManager.java` - Firebase integration

### Model Classes (5 new)
- `src/main/java/com/abir/demo/models/User.java`
- `src/main/java/com/abir/demo/models/Post.java`
- `src/main/java/com/abir/demo/models/Event.java`
- `src/main/java/com/abir/demo/models/Club.java`
- `src/main/java/com/abir/demo/models/Message.java`

### Documentation (3 new)
- `IMPLEMENTATION_SUMMARY.md` - Complete overview of all changes
- `FEATURES_GUIDE.md` - Detailed feature documentation
- `FIREBASE_SETUP.md` - Firebase configuration guide

## 🎨 UI Features

✅ Dark theme with gradient background  
✅ Smooth fade animations between scenes  
✅ Glassmorphism design effects  
✅ Responsive layout  
✅ Color-coded buttons and sections  
✅ Sample data pre-loaded  

## ⚙️ How to Use Firebase Manager

The `FirebaseManager` class handles all database operations:

```java
// Create a user
FirebaseManager.createUser("userId", "email@uni.edu", "Name", "University");

// Create a post
FirebaseManager.createPost("userId", "Hello everyone!", "");

// Send a message
FirebaseManager.sendMessage("userId1", "userId2", "Hi there!");

// Create an event
FirebaseManager.createEvent("Event Name", "Description", "2026-03-15", "Location");

// Create a club
FirebaseManager.createClub("Club Name", "Description", "Category");
```

## 🔄 Scene Navigation

All scenes are connected with the `SceneManager`:

```java
SceneManager.switchScene("dashboard.fxml"); // Go to dashboard
SceneManager.switchScene("profile.fxml");   // Go to profile
SceneManager.switchScene("events.fxml");    // Go to events
// etc.
```

## 🐛 Troubleshooting

### App crashes on startup?
- Verify `Main.java` is using `SceneManager.setPrimaryStage(stage)`
- Check that all FXML files are in `src/main/resources/fxml/`

### Firebase operations not working?
- Verify Firebase Realtime Database is created
- Check that API key is correct in `FirebaseManager.java`
- Make sure database rules allow read/write

### Scene won't load?
- Check controller name matches `fx:controller` in FXML
- Verify FXML file naming (lowercase, .fxml extension)
- Check for typos in controller method names

## 📊 Database Structure

Data is automatically saved to Firebase in this structure:

```
campasian/
├── users/{userId}/ - User profiles
├── posts/{postId}/ - Feed posts
├── events/{eventId}/ - University events
├── clubs/{clubId}/ - Clubs and groups
└── messages/{userId1}_{userId2}/{messageId}/ - Direct messages
```

## 🔐 Sample Credentials (Demo)

For testing, use any username/password:
- **Username**: any text
- **Password**: any text
- **Remember Me**: optional

## 📝 Next: Implement Real Authentication

To add real Firebase Authentication:

1. Enable Authentication in Firebase Console
2. Add `FirebaseAuth` to dependencies
3. Replace mock login with Firebase Auth
4. Store user session in `SharedPreferences`

## 💡 Tips

- Sample data is pre-loaded in each scene
- All buttons are functional (messages go to Firebase)
- You can create real events, clubs, and posts
- Profiles are editable
- Messages are persistent in Firebase

## 🎓 Learning Path

1. **First**: Run the app and explore all 6 scenes
2. **Second**: Check [FEATURES_GUIDE.md](FEATURES_GUIDE.md) for detailed features
3. **Third**: Review [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for database setup
4. **Fourth**: Explore the code in `controllers/` to understand MVC pattern
5. **Fifth**: Customize colors and styling in FXML files

## 📞 Support Files

- **Full Implementation Details**: See `IMPLEMENTATION_SUMMARY.md`
- **Feature Documentation**: See `FEATURES_GUIDE.md`
- **Firebase Setup**: See `FIREBASE_SETUP.md`

## ✅ Checklist Before Going Live

- [ ] Test all 6 scenes and navigation
- [ ] Enable Firebase Realtime Database
- [ ] Set up Firebase Authentication
- [ ] Test creating posts, events, clubs
- [ ] Test messaging functionality
- [ ] Test profile editing
- [ ] Verify Firebase rules are correct
- [ ] Add error handling for failed requests
- [ ] Collect user feedback
- [ ] Deploy to production

## 🎉 You're All Set!

Your Campasian app is ready to run with:
- ✅ 6 new scenes
- ✅ Database integration
- ✅ Model classes
- ✅ Complete documentation
- ✅ Sample data

**Happy coding!** 🚀

---

**Version**: 1.0.0  
**Status**: Ready for Testing  
**Last Updated**: February 18, 2026
