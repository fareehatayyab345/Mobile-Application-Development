# SafeFall - Smart Fall Detection & Emergency Alert System
A secure and reliable Android application for real-time fall detection and automated emergency guardian alerts.

## About the App
SafeFall is a safety-focused Android application designed to protect elderly individuals and those with medical risks by detecting falls automatically using device sensors. The app solves the problem of delayed emergency response by instantly notifying caretakers with the user's exact GPS location via a unique ID-based cloud system. It features a reliable background monitoring service, a manual SOS trigger, and a comprehensive history log for tracking past events. This solution provides essential peace of mind to both vulnerable users and their dedicated guardians.

## App Screenshots
| Splash Screen | Dashboard | Fall Alert | Safety Logs |
|--------------|--------------|-----------|-------------|
| ![Splash](screenshots/splash.png) | ![Dashboard](screenshots/dashboard.png) | ![Alert](screenshots/alert.png) | ![Logs](screenshots/history.png) |

## Features
- **Real-time Fall Detection:** Utilizes accelerometer sensors to detect sudden impacts and stationary states automatically.
- **Unique ID Linking:** Secure connection between user and caretaker through unique safety identification codes.
- **Manual SOS Button:** One-tap emergency trigger for immediate assistance in any hazardous situation.
- **Instant Cloud Notifications:** High-reliability alerts sent via Firebase Cloud Messaging (FCM).
- **GPS Location Sharing:** Automatically generates and sends Google Maps links during emergency events.
- **Event History Logs:** Local database to maintain records of all past incidents with deletion management.
- **User-Friendly Interface:** Modern and clean Material 3 design optimized for accessibility.

## Technologies Used
- **Java:** Main programming language for application logic and services.
- **Android Studio:** Primary development environment and build tool.
- **XML:** Designing responsive user interfaces and layouts.
- **Firebase:** Cloud Messaging for cross-device emergency alerts.
- **SQLite:** Local database for storing user settings and alert history.
- **Google Play Services:** Fused Location Provider API for precise GPS tracking.
- **Gradle:** Build system and dependency management.

## APK Download
[Download SafeFall APK](apk/SafeFall.apk)

## How to Install the APK
1. Download the APK file from the download link provided.
2. Open the APK file on your Android mobile phone.
3. Allow "Installation from unknown sources" in your security settings if prompted.
4. Click "Install" and wait for the process to complete.
5. Run the application and grant the necessary permissions.

## How to Run the Project
1. Clone or download this project repository to your computer.
2. Open the project folder in **Android Studio**.
3. Sync the project with Gradle files to resolve all dependencies.
4. Connect your Firebase project and add the `google-services.json` to the `app/` directory.
5. Add your Firebase Service Account JSON to `app/src/main/res/raw/service_account.json`.
6. Run the app on an Android physical device or an emulator.

## Demo Video
[Watch Demo Video](https://github.com/yourusername/falldetectionapp/demo)

## Privacy Policy
[View Privacy Policy](docs/privacy_policy.pdf)

## Future Enhancements
- **Machine Learning Integration:** To further improve fall detection accuracy and minimize false alarms.
- **Wearable Device Support:** Expanding compatibility to smartwatches for enhanced sensor monitoring.
- **Voice Triggering:** Adding support for voice-activated emergency alerts (e.g., "Help!").
- **Health Vitals Integration:** Monitoring heart rate and oxygen levels alongside fall detection.

## Developed By
- **Student Name:** [Your Name]
- **Class / Semester:** [Your Class/Semester]
- **Department Name:** [Your Department]
- **GitHub:** [Your GitHub Profile Link]
- **LinkedIn:** [Your LinkedIn Profile Link]
