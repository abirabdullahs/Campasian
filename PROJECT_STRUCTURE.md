# Campasian - Complete Project Structure

```
demo/
│
├── 📂 src/main/java/com/campasian/
│   ├── 🚀 CampasianApplication.java          # Main entry point - Application launcher
│   ├── 🎨 Splashanimation.java               # Splash screen with animated "Campasian" title
│   ├── 🎨 SplashScreen.java                  # Additional splash screen utilities
│   │
│   ├── 📂 config/
│   │   └── SupabaseConfig.java               # Supabase database configuration & API keys
│   │
│   ├── 📂 controller/                        # FXML Controllers (30+ controllers)
│   │   ├── 🏠 HomeController.java            # Main home/dashboard layout controller
│   │   ├── 📰 FeedController.java            # Feed/timeline view controller
│   │   ├── 👤 ProfileController.java         # User profile view & editing
│   │   ├── 🔔 NotificationsController.java   # Notifications view controller
│   │   ├── 💬 ChatController.java            # Direct messaging controller
│   │   ├── 🤝 PeopleController.java          # People discovery/search controller
│   │   ├── 👥 FriendListController.java      # Friend list display controller
│   │   ├── 📬 FriendRequestsController.java  # Friend requests management controller
│   │   ├── 🏘️ CommunityController.java       # Community rooms/groups controller
│   │   ├── 🎓 FacultyController.java         # Faculty directory controller
│   │   ├── 🎉 EventsController.java          # Events management controller
│   │   ├── 💰 MarketplaceController.java     # Marketplace items controller
│   │   ├── 📚 ResourceController.java        # Course resources controller
│   │   ├── 🤝 StudyPartnerController.java    # Study partner matching controller
│   │   ├── 😶 ConfessionController.java      # Anonymous confessions controller
│   │   ├── 📍 LostFoundController.java       # Lost & Found items controller
│   │   ├── 🩸 BloodSearchController.java     # Blood donor search controller
│   │   ├── ⚙️ SettingsController.java        # User settings controller
│   │   ├── 🔑 LoginController.java           # Login page controller
│   │   ├── 📝 SignupController.java          # Registration page controller
│   │   ├── 👋 HelloController.java           # Welcome/intro controller
│   │   ├── 📝 PostEditorModalController.java # Post creation modal
│   │   ├── ✏️ EditProfileModalController.java# Profile editing modal
│   │   ├── 💬 CreateRoomModalController.java # Community room creation modal
│   │   ├── 🎊 EventAddModalController.java   # Event creation modal
│   │   ├── 📍 LostFoundModalController.java  # Lost item report modal
│   │   ├── 💵 SellItemModalController.java   # Sell item modal
│   │   ├── 📚 ResourceAddModalController.java# Add resource modal
│   │   ├── 🤝 StudyPartnerModalController.java# Study partner post modal
│   │   └── 😶 ConfessionModalController.java # Anonymous confession modal
│   │
│   ├── 📂 model/                             # Data models (19 models)
│   │   ├── 👤 UserProfile.java              # User profile data model
│   │   ├── 👥 User.java                     # Basic user model
│   │   ├── 📝 Post.java                     # Feed post model
│   │   ├── 💬 Comment.java                  # Comment model
│   │   ├── 🎫 FriendRequest.java            # Friend request model
│   │   ├── 💌 Message.java                  # Direct message model
│   │   ├── 🔔 Notification.java             # Notification model
│   │   ├── 🏘️ CommunityRoom.java            # Community room model
│   │   ├── 💬 CommunityMessage.java         # Community chat message model
│   │   ├── 🎉 CampusEvent.java              # Event model
│   │   ├── 🤝 StudyPartnerPost.java         # Study partner post model
│   │   ├── 😶 Confession.java               # Anonymous confession model
│   │   ├── 📍 LostFoundItem.java            # Lost & Found item model
│   │   ├── 💰 MarketplaceItem.java          # Marketplace item model
│   │   ├── 📚 CourseResource.java           # Course resource model
│   │   ├── 🎓 Faculty.java                  # Faculty member model
│   │   ├── ⭐ FacultyFeedback.java          # Faculty feedback/rating model
│   │   ├── 📞 CallRecord.java               # Call history model
│   │   └── 📦 package-info.java             # Package documentation
│   │
│   ├── 📂 service/                          # Business logic & API services (8 services)
│   │   ├── 🔗 ApiService.java               # Main REST API service (2000+ lines)
│   │   │                                      # - User authentication
│   │   │                                      # - CRUD operations for all entities
│   │   │                                      # - Search & filtering
│   │   │                                      # - File uploads to Supabase Storage
│   │   ├── 🔐 AuthService.java              # Authentication & session management
│   │   ├── 💳 TokenManager.java             # JWT token management
│   │   ├── 🎓 UniversityService.java        # University lookup & data
│   │   ├── 🏘️ CommunityService.java         # Community/group management
│   │   ├── ⚡ SupabaseRealtimeService.java  # Real-time WebSocket service
│   │   ├── 📱 BrowserCallBridgeService.java # Browser integration for video calls
│   │   └── ❌ ApiException.java             # Custom API exception class
│   │
│   ├── 📂 util/                             # Utility classes
│   │   └── 🖼️ ImageSelectionSupport.java   # Image selection & processing
│   │
│   └── 📂 view/                             # Navigation & routing
│       ├── 🗺️ ViewPaths.java               # All FXML file path constants
│       ├── 🧭 SceneManager.java            # Scene management & navigation
│       ├── 🔗 AppRouter.java               # Application-level routing
│       └── 📍 NavigationContext.java       # Navigation state management
│
├── 📂 src/main/resources/
│   ├── 📂 fxml/                            # UI Layout files (30+ FXML files)
│   │   ├── 🏠 home-view.fxml               # Main home layout with sidebar
│   │   ├── 📰 feed-view.fxml               # Feed/timeline view
│   │   ├── 👤 profile-view.fxml            # User profile view
│   │   ├── 🔔 notifications-view.fxml      # Notifications list
│   │   ├── 💬 chat-view.fxml               # Direct messaging interface
│   │   ├── 🤝 people-view.fxml             # People discovery/search
│   │   ├── 👥 friend-list-view.fxml        # Friends list display
│   │   ├── 📬 friend-requests-view.fxml    # Friend requests management
│   │   ├── 🏘️ community-view.fxml          # Community rooms interface
│   │   ├── 🎓 faculty-view.fxml            # Faculty directory
│   │   ├── 🎉 events-view.fxml             # Events listing
│   │   ├── 💰 marketplace-view.fxml        # Marketplace items
│   │   ├── 📚 resource-view.fxml           # Course resources
│   │   ├── 🤝 study-partner-view.fxml      # Study partner matching
│   │   ├── 😶 confession-view.fxml         # Confessions feed
│   │   ├── 📍 lost-found-view.fxml         # Lost & Found items
│   │   ├── 🩸 blood-search-view.fxml       # Blood donor search
│   │   ├── ⚙️ settings-view.fxml           # User settings
│   │   ├── 🔑 login.fxml                   # Login form
│   │   ├── 📝 signup.fxml                  # Registration form
│   │   ├── 👋 hello-view.fxml              # Welcome screen
│   │   ├── 🏠 home.fxml                    # Alternative home layout
│   │   ├── 📝 post-editor-modal.fxml       # Create post modal
│   │   ├── ✏️ edit-profile-modal.fxml      # Edit profile modal
│   │   ├── 💬 create-room-modal.fxml       # Create community room modal
│   │   ├── 🎊 event-add-modal.fxml         # Create event modal
│   │   ├── 📍 lost-found-modal.fxml        # Report lost item modal
│   │   ├── 💵 sell-item-modal.fxml         # Sell marketplace item modal
│   │   ├── 📚 resource-add-modal.fxml      # Add course resource modal
│   │   ├── 🤝 study-partner-modal.fxml     # Create study partner post modal
│   │   └── 😶 confession-modal.fxml        # Submit confession modal
│   │
│   ├── 📂 styles/                          # CSS Styling (23+ CSS files)
│   │   ├── 🎨 app.css                      # Global app styles
│   │   ├── 🎨 style.css                    # Common component styles
│   │   ├── 🎨 theme.css                    # Theme & color definitions
│   │   ├── 🏠 home.css                     # Home/sidebar styles
│   │   ├── 📰 feed.css                     # Feed view styles
│   │   ├── 👤 profile.css                  # Profile view styles
│   │   ├── 🔔 notifications.css            # Notifications styles
│   │   ├── 💬 chat.css                     # Chat interface styles
│   │   ├── 🤝 people.css                   # People discovery styles
│   │   ├── 👥 friends.css                  # Friend list styles
│   │   ├── 📬 friend-requests.css          # Friend requests styles
│   │   ├── 🏘️ community.css                # Community styles
│   │   ├── 🎓 faculty.css                  # Faculty directory styles
│   │   ├── 🎉 events.css                   # Events view styles
│   │   ├── 💰 marketplace.css              # Marketplace styles
│   │   ├── 📚 resource.css                 # Resources view styles
│   │   ├── 🤝 study-partner.css            # Study partner styles
│   │   ├── 😶 confession.css               # Confessions styles
│   │   ├── 📍 lost-found.css               # Lost & Found styles
│   │   ├── 🔑 signup.css                   # Login/signup styles
│   │   ├── ⚙️ settings.css                 # Settings view styles
│   │   └── 📝 post-editor.css              # Post editor styles
│   │
│   ├── 📂 database/                        # Database configuration
│   │   ├── 📊 university.json              # University data (100+ universities)
│   │   └── 📂 db/
│   │       ├── 👤 profiles_table.sql       # User profiles table schema
│   │       ├── 📝 posts_table.sql          # Posts table schema
│   │       ├── 🎯 social_schema.sql        # Social features schema
│   │       ├── 📋 social_extensions.sql    # Extended social features
│   │       ├── 🏘️ campus_ecosystem.sql     # Community/events/marketplace
│   │       ├── 🎓 academic_community.sql   # Faculty/resources/study partners
│   │       ├── 📞 calls_table.sql          # Call records schema
│   │       ├── ⚡ advanced_features.sql    # Notifications & triggers
│   │       ├── 🖼️ storage_setup.sql        # Supabase Storage configuration
│   │       └── 📂 migration/
│   │           └── V1__create_users_table.sql  # Database migrations
│   │
│   ├── 📂 images/                          # Application assets
│   │   ├── 🎨 app-icon.ico                 # Application icon
│   │   ├── 🖼️ loginPage.jpeg               # Login page background
│   │   ├── 🖼️ socialMedia.jpeg             # Social media promo image
│   │   └── 🖼️ varsity.jpg                  # University/campus image
│   │
│   ├── 📂 config/                          # Configuration files
│   │   └── ⚙️ (configuration resources)
│   │
│   ├── 📂 styles/                          # Additional styles directory
│   │   └── (theme files)
│   │
│   └── 📂 web/                             # Web resources
│       └── (web-related assets)
│
├── 📂 build/                               # Build output directory
│   ├── 📂 classes/                         # Compiled Java classes
│   ├── 📂 libs/
│   │   ├── campasian-1.0-SNAPSHOT.jar      # Main application JAR
│   │   └── (dependency JARs)
│   ├── 📂 distributions/
│   │   ├── campasian-1.0-SNAPSHOT.tar      # TAR distribution
│   │   └── campasian-1.0-SNAPSHOT.zip      # ZIP distribution
│   ├── 📂 reports/
│   │   └── 📊 problems/                    # Build problem reports
│   ├── 📂 resources/                       # Compiled resources
│   ├── 📂 scripts/
│   │   ├── campasian                       # Linux/Mac run script
│   │   └── campasian.bat                   # Windows run script
│   └── 📂 tmp/                             # Temporary build files
│
├── 📂 gradle/
│   └── 📂 wrapper/
│       ├── gradle-wrapper.jar              # Gradle wrapper JAR
│       └── gradle-wrapper.properties       # Gradle version configuration
│
├── 📂 docs/
│   └── 📂 sql/
│       └── community_schema.sql            # Community schema documentation
│
├── ⚙️ build.gradle.kts                     # Gradle build configuration
│   │                                        # - Dependencies (JavaFX, Supabase, Gson)
│   │                                        # - Plugins (JavaFX, application)
│   │                                        # - Java 21 compatibility
│   │                                        # - Custom run/package tasks
│
├── ⚙️ settings.gradle.kts                  # Gradle settings
│   └── rootProject.name = "campasian"
│
├── 🔧 gradlew                              # Gradle wrapper script (Unix)
├── 🔧 gradlew.bat                          # Gradle wrapper script (Windows)
├── 📋 README.md                            # Project documentation
├── 📋 PROJECT_STRUCTURE.md                 # This file
└── 📄 demo.iml                             # IntelliJ IDEA project file

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

