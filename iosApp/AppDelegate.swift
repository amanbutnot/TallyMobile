//
// Created by Aman Saini on 10/09/26.
//
import UIKit
import FirebaseCore
import FirebaseMessaging
import UserNotifications
import Foundation
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        FirebaseApp.configure()
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { granted, _ in }
        application.registerForRemoteNotifications()
        Messaging.messaging().delegate = self

        NotificationCenter.default.addObserver(
            forName: NSNotification.Name("RemoveFirebaseTokenNotification"),
            object: nil,
            queue: .main
        ) { _ in
            Messaging.messaging().deleteToken { error in
                if let error = error {
                    print("Error deleting FCM token: \(error)")
                } else {
                    print("FCM token deleted successfully")
                }
            }
        }

        return true
    }
    // This is the bridge: Apple gives you a raw APNs token,
    // you hand it to Firebase so it can map it to an FCM token.
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken
    }
}
extension AppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        UserDefaults.standard.set(token, forKey: "fcm_token")
        print("FCM Token: \(token)")
    }
}
