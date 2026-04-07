# Campasian - Campus Social Network

<div align="center">

![Campasian](https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21%2B-orange?style=for-the-badge)
![JavaFX](https://img.shields.io/badge/JavaFX-21-green?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)

**Your Campus, Your Connections** 

A modern, high-fidelity desktop application for campus social networking, built with JavaFX and designed with Shadcn/UI & Linear.app aesthetics.

[Features](#-features) • [Installation](#-installation) • [Architecture](#-architecture) • [UI/UX](#-modern-ui--ux) • [Contributing](#-contributing)

</div>

---

## Overview

Campasian is a sophisticated desktop application that transforms campus connectivity through a clean, professional social networking platform. Built for students to create posts, follow peers, engage in discussions, and stay updated with campus life—all in a beautiful, Modern interface inspired by leading design systems.

### Vision
Create a thriving campus community where students can:
- Share academic proposals and campus insights
- Connect with peers across departments  
- Engage through likes, comments, and discussions
- Stay Informed with real-time notifications
- Discover trending campus topics

---

## 🚀 Features

### Core Features
- User Authentication - Secure sign-up and login with email verification
- Feed & Timeline - Global and personalized feed with infinite scroll
- Post Creation - Beautiful modal for sharing thoughts and proposals
- Social Interactions - Like, comment, and engage with community posts
- User Profiles - Complete profile management with university info
- Follow System - Build your network by following peers
- Notifications - Real-time alerts for engagement and interactions
- University Directory - Search and connect with students from any university

### UI/UX Excellence
- Shadcn/UI Inspired - Clean, modern design language
- Professional Aesthetics - Linear.app style interface
- Responsive Layout - Adaptive design for all screen sizes
- Beautiful States - Empty states and loading states
- Smooth Interactions - Subtle animations and hover effects
- Modern Color Palette - Electric blue accents with soft backgrounds

---

## 🏗️ Architecture

### Technology Stack

```
┌─────────────────────────────────────────────────────┐
│              Campasian Tech Stack                  │
├─────────────────────────────────────────────────────┤
│  Frontend       │ JavaFX 21 + FXML              │
│  Styling        │ CSS (Shadcn-inspired)         │
│  Backend        │ Supabase / PostgreSQL         │
│  Authentication │ Supabase Auth                 │
│  Build Tool     │ Gradle                        │
│  Java Version   │ Java 21+                      │
└─────────────────────────────────────────────────────┘
```

### Project Structure

# Campasian - Complete Project Structure

```
demo/
│
├── src/main/java/com/campasian/
│   ├── CampasianApplication.java          # Main entry point - Application launcher
│   ├── Splashanimation.java               # Splash screen with animated "Campasian" title
│   ├── SplashScreen.java                  # Additional splash screen utilities
│   │
│   ├── config/
│   │   └── SupabaseConfig.java               # Supabase database configuration & API keys
│   │
│   ├── controller/                        # FXML Controllers (30+ controllers)
│   │   ├── HomeController.java            # Main home/dashboard layout controller
│   │   ├── FeedController.java            # Feed/timeline view controller
│   │   ├── ProfileController.java         # User profile view & editing
│   │   ├── NotificationsController.java   # Notifications view controller
│   │   ├── ChatController.java            # Direct messaging controller
│   │   ├── PeopleController.java          # People discovery/search controller
│   │   ├── FriendListController.java      # Friend list display controller
│   │   ├── FriendRequestsController.java  # Friend requests management controller
│   │   ├── CommunityController.java       # Community rooms/groups controller
│   │   ├── FacultyController.java         # Faculty directory controller
│   │   ├── EventsController.java          # Events management controller
│   │   ├── MarketplaceController.java     # Marketplace items controller
│   │   ├── ResourceController.java        # Course resources controller
│   │   ├── StudyPartnerController.java    # Study partner matching controller
│   │   ├── ConfessionController.java      # Anonymous confessions controller
│   │   ├── LostFoundController.java       # Lost & Found items controller
│   │   ├── BloodSearchController.java     # Blood donor search controller
│   │   ├── SettingsController.java        # User settings controller
│   │   ├── LoginController.java           # Login page controller
│   │   ├── SignupController.java          # Registration page controller
│   │   ├── HelloController.java           # Welcome/intro controller
│   │   ├── PostEditorModalController.java # Post creation modal
│   │   ├── EditProfileModalController.java# Profile editing modal
│   │   ├── CreateRoomModalController.java # Community room creation modal
│   │   ├── EventAddModalController.java   # Event creation modal
│   │   ├── LostFoundModalController.java  # Lost item report modal
│   │   ├── SellItemModalController.java   # Sell item modal
│   │   ├── ResourceAddModalController.java# Add resource modal
│   │   ├── StudyPartnerModalController.java# Study partner post modal
│   │   └── ConfessionModalController.java # Anonymous confession modal
│   │
│   ├── model/                             # Data models (19 models)
│   │   ├── UserProfile.java              # User profile data model
│   │   ├── User.java                     # Basic user model
│   │   ├── Post.java                     # Feed post model
│   │   ├── Comment.java                  # Comment model
│   │   ├── FriendRequest.java            # Friend request model
│   │   ├── Message.java                  # Direct message model
│   │   ├── Notification.java             # Notification model
│   │   ├── CommunityRoom.java            # Community room model
│   │   ├── CommunityMessage.java         # Community chat message model
│   │   ├── CampusEvent.java              # Event model
│   │   ├── StudyPartnerPost.java         # Study partner post model
│   │   ├── Confession.java               # Anonymous confession model
│   │   ├── LostFoundItem.java            # Lost & Found item model
│   │   ├── MarketplaceItem.java          # Marketplace item model
│   │   ├── CourseResource.java           # Course resource model
│   │   ├── Faculty.java                  # Faculty member model
│   │   ├── FacultyFeedback.java          # Faculty feedback/rating model
│   │   ├── CallRecord.java               # Call history model
│   │   └── package-info.java             # Package documentation
│   │
│   ├── service/                          # Business logic & API services (8 services)
│   │   ├── ApiService.java               # Main REST API service (2000+ lines)
│   │   │                                      # - User authentication
│   │   │                                      # - CRUD operations for all entities
│   │   │                                      # - Search & filtering
│   │   │                                      # - File uploads to Supabase Storage
│   │   ├── AuthService.java              # Authentication & session management
│   │   ├── TokenManager.java             # JWT token management
│   │   ├── UniversityService.java        # University lookup & data
│   │   ├── CommunityService.java         # Community/group management
│   │   ├── SupabaseRealtimeService.java  # Real-time WebSocket service
│   │   ├── BrowserCallBridgeService.java # Browser integration for video calls
│   │   └── ApiException.java             # Custom API exception class
│   │
│   ├── util/                             # Utility classes
│   │   └── ImageSelectionSupport.java   # Image selection & processing
│   │
│   └── view/                             # Navigation & routing
│       ├── ViewPaths.java               # All FXML file path constants
│       ├── SceneManager.java            # Scene management & navigation
│       ├── AppRouter.java               # Application-level routing
│       └── NavigationContext.java       # Navigation state management
│
├── src/main/resources/
│   ├── fxml/                            # UI Layout files (30+ FXML files)
│   │   ├── home-view.fxml               # Main home layout with sidebar
│   │   ├── feed-view.fxml               # Feed/timeline view
│   │   ├── profile-view.fxml            # User profile view
│   │   ├── notifications-view.fxml      # Notifications list
│   │   ├── chat-view.fxml               # Direct messaging interface
│   │   ├── people-view.fxml             # People discovery/search
│   │   ├── friend-list-view.fxml        # Friends list display
│   │   ├── friend-requests-view.fxml    # Friend requests management
│   │   ├── community-view.fxml          # Community rooms interface
│   │   ├── faculty-view.fxml            # Faculty directory
│   │   ├── events-view.fxml             # Events listing
│   │   ├── marketplace-view.fxml        # Marketplace items
│   │   ├── resource-view.fxml           # Course resources
│   │   ├── study-partner-view.fxml      # Study partner matching
│   │   ├── confession-view.fxml         # Confessions feed
│   │   ├── lost-found-view.fxml         # Lost & Found items
│   │   ├── blood-search-view.fxml       # Blood donor search
│   │   ├── settings-view.fxml           # User settings
│   │   ├── login.fxml                   # Login form
│   │   ├── signup.fxml                  # Registration form
│   │   ├── hello-view.fxml              # Welcome screen
│   │   ├── home.fxml                    # Alternative home layout
│   │   ├── post-editor-modal.fxml       # Create post modal
│   │   ├── edit-profile-modal.fxml      # Edit profile modal
│   │   ├── create-room-modal.fxml       # Create community room modal
│   │   ├── event-add-modal.fxml         # Create event modal
│   │   ├── lost-found-modal.fxml        # Report lost item modal
│   │   ├── sell-item-modal.fxml         # Sell marketplace item modal
│   │   ├── resource-add-modal.fxml      # Add course resource modal
│   │   ├── study-partner-modal.fxml     # Create study partner post modal
│   │   └── confession-modal.fxml        # Submit confession modal
│   │
│   ├── styles/                          # CSS Styling (23+ CSS files)
│   │   ├── app.css                      # Global app styles
│   │   ├── style.css                    # Common component styles
│   │   ├── theme.css                    # Theme & color definitions
│   │   ├── home.css                     # Home/sidebar styles
│   │   ├── feed.css                     # Feed view styles
│   │   ├── profile.css                  # Profile view styles
│   │   ├── notifications.css            # Notifications styles
│   │   ├── chat.css                     # Chat interface styles
│   │   ├── people.css                   # People discovery styles
│   │   ├── friends.css                  # Friend list styles
│   │   ├── friend-requests.css          # Friend requests styles
│   │   ├── community.css                # Community styles
│   │   ├── faculty.css                  # Faculty directory styles
│   │   ├── events.css                   # Events view styles
│   │   ├── marketplace.css              # Marketplace styles
│   │   ├── resource.css                 # Resources view styles
│   │   ├── study-partner.css            # Study partner styles
│   │   ├── confession.css               # Confessions styles
│   │   ├── lost-found.css               # Lost & Found styles
│   │   ├── signup.css                   # Login/signup styles
│   │   ├── settings.css                 # Settings view styles
│   │   └── post-editor.css              # Post editor styles
│   │
│   ├── database/                        # Database configuration
│   │   ├── university.json              # University data (100+ universities)
│   │   └── db/
│   │       ├── profiles_table.sql       # User profiles table schema
│   │       ├── posts_table.sql          # Posts table schema
│   │       ├── social_schema.sql        # Social features schema
│   │       ├── social_extensions.sql    # Extended social features
│   │       ├── campus_ecosystem.sql     # Community/events/marketplace
│   │       ├── academic_community.sql   # Faculty/resources/study partners
│   │       ├── calls_table.sql          # Call records schema
│   │       ├── advanced_features.sql    # Notifications & triggers
│   │       ├── storage_setup.sql        # Supabase Storage configuration
│   │       └── migration/
│   │           └── V1__create_users_table.sql  # Database migrations
│   │
│   ├── images/                          # Application assets
│   │   ├── app-icon.ico                 # Application icon
│   │   ├── loginPage.jpeg               # Login page background
│   │   ├── socialMedia.jpeg             # Social media promo image
│   │   └── varsity.jpg                  # University/campus image
│   │
│   ├── config/                          # Configuration files
│   │   └── (configuration resources)
│   │
│   ├── styles/                          # Additional styles directory
│   │   └── (theme files)
│   │
│   └── web/                             # Web resources
│       └── (web-related assets)
│
├── build/                               # Build output directory
│   ├── classes/                         # Compiled Java classes
│   ├── libs/
│   │   ├── campasian-1.0-SNAPSHOT.jar      # Main application JAR
│   │   └── (dependency JARs)
│   ├── distributions/
│   │   ├── campasian-1.0-SNAPSHOT.tar      # TAR distribution
│   │   └── campasian-1.0-SNAPSHOT.zip      # ZIP distribution
│   ├── reports/
│   │   └── problems/                    # Build problem reports
│   ├── resources/                       # Compiled resources
│   ├── scripts/
│   │   ├── campasian                       # Linux/Mac run script
│   │   └── campasian.bat                   # Windows run script
│   └── tmp/                             # Temporary build files
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar              # Gradle wrapper JAR
│       └── gradle-wrapper.properties       # Gradle version configuration
│
├── docs/
│   └── sql/
│       └── community_schema.sql            # Community schema documentation
│
├── build.gradle.kts                     # Gradle build configuration
│   │                                        # - Dependencies (JavaFX, Supabase, Gson)
│   │                                        # - Plugins (JavaFX, application)
│   │                                        # - Java 21 compatibility
│   │                                        # - Custom run/package tasks
│
├── settings.gradle.kts                  # Gradle settings
│   └── rootProject.name = "campasian"
│
├── gradlew                              # Gradle wrapper script (Unix)
├── gradlew.bat                          # Gradle wrapper script (Windows)
├── README.md                            # Project documentation
├── PROJECT_STRUCTURE.md                 # This file
└── demo.iml                             # IntelliJ IDEA project file

```

---

## Key Features By Directory

### Controllers (30 Controllers)
- **Authentication**: LoginController, SignupController
- **Main UI**: HomeController, HelloController
- **Content**: FeedController, ProfileController, PeopleController
- **Social**: ChatController, NotificationsController, FriendListController, FriendRequestsController
- **Communities**: CommunityController, StudyPartnerController, ConfessionController
- **Marketplace**: MarketplaceController, LostFoundController, BloodSearchController
- **Academic**: FacultyController, EventsController, ResourceController
- **Settings**: SettingsController
- **Modals** (11): For creating/editing posts, profiles, rooms, events, items, etc.

### Models (19 Data Models)
- **Core**: User, UserProfile
- **Social**: Post, Comment, FriendRequest, Message
- **Community**: CommunityRoom, CommunityMessage
- **Special**: Confession, StudyPartnerPost, LostFoundItem
- **Business**: MarketplaceItem, CampusEvent, CourseResource
- **System**: Notification, CallRecord, Faculty, FacultyFeedback

### Services (8 Services)
- **ApiService**: Comprehensive REST API wrapper (2000+ lines)
- **AuthService**: Authentication & session management
- **TokenManager**: JWT token handling
- **CommunityService**: Community features
- **SupabaseRealtimeService**: Real-time updates via WebSocket
- **BrowserCallBridgeService**: Video call integration
- **UniversityService**: University lookup
- **ApiException**: Error handling

### Styling (23 CSS Files)
- **Global**: app.css, style.css, theme.css
- **Views**: One CSS per major view (feed, profile, chat, etc.)
- **Total**: 1000+ lines of custom CSS styling

### Database (11 SQL Files)
- **Schema**: profiles, posts, social, communities, events, marketplace
- **Features**: Notifications, triggers, RLS policies
- **Storage**: Supabase Storage setup
- **Migrations**: Versioned database migrations

---

## Technology Stack

- **Frontend**: JavaFX 21 + FXML
- **Backend**: Supabase REST API
- **Database**: PostgreSQL (via Supabase)
- **Storage**: Supabase Storage
- **Real-time**: WebSocket (Supabase Realtime)
- **Build**: Gradle 8.0+
- **Java**: Java 21+
- **Styling**: JavaFX CSS

---

## File Statistics

- **Java Files**: 57 files (controllers, models, services, utilities)
- **FXML Files**: 30 UI layout files
- **CSS Files**: 23 stylesheet files
- **SQL Files**: 11 database schema files
- **Total Code**: 10,000+ lines of code
- **Controllers**: 30 FXML controllers
- **Models**: 19 data models
- **Services**: 8 business logic services

---

## 🎨 Modern UI/UX Design

### Design System

#### Color Palette
```
Primary Background    #F9FAFB    (Soft Gray)
Surface              #FFFFFF    (Clean White)
Primary Text         #18181B    (Deep Black)
Secondary Text       #71717A    (Medium Gray)
Accent Color         #3B82F6    (Electric Blue)
Border Color         #E4E4E7    (Light Gray)
Error Color          #EF4444    (Red)
```

#### Typography
- Font Stack: Inter, Poppins, Segoe UI, system-ui, sans-serif
- Headings: 700 weight bold (28px-20px)
- Body: 400 weight regular (14px)
- Labels: 500 weight medium (12px-13px)

#### Component Design

Cards & Containers
- Border Radius: 8px
- Padding: 20px
- Box Shadow: `0 0 rgba(0,0,0,0.08) 4px blur`
- Hover Effect: Enhanced shadow on interaction

Buttons
- Primary: Deep black (#18181B) with white text
- Secondary: Transparent border with hover background
- Border Radius: 8px
- Padding: 11px 24px
- Focus State: Blue glow effect

Inputs & TextAreas
- Border Color: #E4E4E7
- Focus Border: #3B82F6 with electric glow
- Border Radius: 8px
- Padding: 12px 16px

Sidebar Navigation
- Background: #FFFFFF
- Active State: 2px left border with light background
- User Card: Circular avatar with name below
- Smooth hover transitions

---

## Key Pages & Features

### Home
- Clean sidebar navigation with user profile mini-card
- Dynamic content area for sub-views
- Floating user profile at bottom of sidebar

### Feed
- Create post button with beautiful modal
- Global/Following filter tabs with active state underline
- Trending Topics sidebar (right panel)
- Post cards with engagement metrics
- Smooth scrolling experience

### Profile
- Cover photo section (140px hero image)
- Profile stats (Followers, Following)
- University & student information
- Editable user details

### Notifications
- Beautiful empty state: "All caught up!"
- Notification cards with metadata
- Smooth transitions
- Clear visual hierarchy

### Community
- Campus groups and communities
- Easy discovery interface
- Future expansion ready

---

## Installation & Setup

### Prerequisites
- Java 21 or higher
- Gradle 8.0+
- Git (for version control)

### Clone the Repository
```bash
git clone https://github.com/abirabdullahs/campasian.git
cd campasian/demo
```

### Build the Project
```bash
./gradlew build
```

### Run the Application
```bash
./gradlew run
```

Or execute the main class directly:
```bash
./gradlew JavaExec
```

### IntelliJ IDEA

1. Open the project in IntelliJ
2. Right-click `build.gradle.kts` → `Load as Gradle Project`
3. Run `CampasianApplication.java`

---

## Usage Guide

### Getting Started

1. Launch Application
   - Run the app and login screen appears
   - Create an account or sign in with credentials

2. Complete Your Profile
   - Fill in full name, university, student ID
   - Add profile picture (avatar auto-generated with initials)

3. Explore the Feed
   - View global posts or switch to following feed
   - Create posts using the "What's on your mind?" button
   - Like and comment on posts you find interesting

4. Connect with Others
   - Visit profiles to see user details
   - Follow/unfollow users
   - Check notifications for new interactions

### Keyboard Shortcuts
- `Ctrl+N` - New Post (when focused)
- `Escape` - Close Modal
- `Ctrl+Q` - Quit Application

---

## Design Inspiration

Campasian draws design inspiration from:
- Shadcn/UI - Component architecture and styling patterns
- Linear.app - Professional minimalism and polish
- Modern Web Design - Clean aesthetics adapted for desktop

### Design Goals Achieved
- Clean sans-serif typography throughout
- Consistent color palette implementation
- Subtle box shadows (0 1px 3px rgba)
- Active state indicators with 2px borders
- User profile mini-card in sidebar
- Trending topics sidebar on feed
- Cover photo section in profile
- Beautiful empty state messaging
- Rounded corners (8px) on all components
- Focus/hover effects with smooth transitions
- Proper spacing and alignment grid (8px)

---

## Security & Data

### Authentication
- Email-based user authentication
- Secure password hashing
- Token-based sessions
- Protected API endpoints

### Data Privacy
- User data encryption in transit
- Secure database connections
- No sensitive data in logs
- GDPR-compliant data handling

---

---

## Database Schema

```sql
-- Users Table
users {
  id (PK),
  email (UNIQUE),
  full_name,
  university,
  student_id,
  profile_picture_url,
  created_at
}

-- Posts Table
posts {
  id (PK),
  user_id (FK),
  content,
  image_url,
  created_at,
  updated_at
}

-- Likes Table
likes {
  id (PK),
  user_id (FK),
  post_id (FK),
  created_at
}

-- Comments Table
comments {
  id (PK),
  user_id (FK),
  post_id (FK),
  content,
  created_at
}

-- Followers Table
followers {
  id (PK),
  follower_id (FK),
  following_id (FK),
  created_at
}

-- Notifications Table
notifications {
  id (PK),
  user_id (FK),
  type (like|comment|follow),
  related_user_id (FK),
  related_post_id (FK),
  read (BOOLEAN),
  created_at
}
```

---

## Testing

### Unit Tests
```bash
./gradlew test
```

### Running Specific Tests
```bash
./gradlew test --tests com.campasian.controller.*
```

---

##  Contributing

We welcome contributions! Here's how:

###  Code Style
-  Follow Google Java Style Guide
-  Use meaningful variable names
-  Add JavaDoc comments for public methods
-  Keep methods focused and testable

###  Commit Messages
```
feat: Add trending topics sidebar
fix: Correct empty state display logic
refactor: Simplify profile card styling
docs: Update README with examples
```

###  Pull Request Process
1.  Fork the repository
2.  Create a feature branch (`git checkout -b feature/amazing-feature`)
3.  Commit changes (`git commit -m 'Add amazing feature'`)
4.  Push to branch (`git push origin feature/amazing-feature`)
5.  Open a Pull Request



##  Known Issues

|  Issue | Status |  Notes |
|-------|--------|-------|
|  Community page coming soon |  Planned | Full implementation in v1.1 |
|  Settings page partial |  In Progress | Will add user preferences |
|  Image uploads |  Planned | Including profile & post images |
|  Real-time notifications |  Planned | WebSocket integration |

---


##  Support & Feedback

###  Getting Help
- Email: contact@abirabdullah.me
- Discord: [Join our community](https://discord.gg/campasian)
- Issues: [GitHub Issues](https://github.com/abirabdullahs/campasian)

### Feature Requests
Have an idea? [Submit a feature request](https://github.com/yourusername/campasian/discussions)

---

## License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

```
MIT License ©️

Copyright (c) 2026 Campasian Contributors 🎓

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

---

## Acknowledgments

- **JavaFX Community** - For the excellent framework
- **Supabase** - Backend services
- **Design Inspiration** - Shadcn/UI and Linear.app
- **Contributors** - Everyone who has helped with this project

---

## Authors

[Abir Abdullah](https://github.com/abirabdullahs) - Initial work

---

<div align="center">

### Made with Java-FX for the Campus Community

⭐ **Star this repo** if you find it helpful!

[⬆ Back to Top](#-campasian---campus-social-network)

</div>
