package com.campasian.view;

import com.campasian.controller.HomeController;

/**
 * Application-level router for navigation between views.
 */
public final class AppRouter {

    private static HomeController homeController;

    public static void setHomeController(HomeController controller) {
        homeController = controller;
    }

    /** Navigate to a user's profile (from People or Post click). */
    public static void navigateToProfile(String userId) {
        if (homeController != null && userId != null) {
            homeController.loadProfileView(userId);
        }
    }
}
