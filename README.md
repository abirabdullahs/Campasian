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

```
demo/
├── src/main/java/com/campasian/
│   ├── CampasianApplication.java       # Main entry point
│   ├── config/
│   │   └── SupabaseConfig.java         # Database configuration
│   ├── controller/                     # FXML Controllers
│   │   ├── HomeController.java
│   │   ├── FeedController.java
│   │   ├── ProfileController.java
│   │   ├── NotificationsController.java
│   │   └── ...
│   ├── model/                          # Data models
│   ├── service/                        # Business logic
│   └── view/
│       ├── SceneManager.java           # Navigation
│       └── ViewPaths.java              # Route constants
│
├── src/main/resources/
│   ├── fxml/                           # UI Layouts
│   │   ├── home-view.fxml
│   │   ├── feed-view.fxml
│   │   ├── profile-view.fxml
│   │   ├── notifications-view.fxml
│   │   ├── post-editor-modal.fxml
│   │   └── ...
│   ├── styles/
│   │   └── app.css                     # Global styles
│   ├── database/
│   │   └── university.json             # University data
│   └── images/                         # Assets
│
├── build.gradle.kts                    # Gradle configuration
└── README.md                           # This file
```

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
git clone https://github.com/yourusername/campasian.git
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

---

##  Documentation

###  User Guide
See [USERGUIDE.md](./USERGUIDE.md) for detailed user documentation.

###  Developer Guide
See [DEVGUIDE.md](./DEVGUIDE.md) for API and architecture details.

###  Design System
See [DESIGN.md](./DESIGN.md) for UI/UX specifications.

---

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
- Email: support@campasian.com
- Discord: [Join our community](https://discord.gg/campasian)
- Issues: [GitHub Issues](https://github.com/yourusername/campasian/issues)

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
