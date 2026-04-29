# LabDigitiser

LabDigitiser is a specialized Android application designed for monitoring water quality. It provides a secure and efficient platform for Industry Operators and Lab Technicians to manage water quality data.

## Features

- **Secure Authentication**: Dedicated login and signup flows for different user roles (Operator, Technician, Admin).
- **Dashboard**: Real-time overview of key metrics like pH, TDS, BOD, and COD.
- **Reports**: Detailed data visualization and historical report tracking.
- **Data Entries**: Easy-to-use forms for recording new water quality measurements.
- **Export Options**: Export data to various formats including PDF and Excel.
- **Admin-Panel Ready Backend**: User accounts are linked to organization records in Firestore so a website admin panel can control plant labels, modules, and app-facing settings.

## Tech Stack

- **Platform**: Android
- **Language**: Java
- **UI Components**: Material Design 3 (Material Components for Android)
- **Architecture**: ViewBinding for robust UI interaction
- **Layouts**: ConstraintLayout, NestedScrollView, and Custom XML Drawables

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio (Koala or newer recommended).
3. Sync Gradle and build the project.
4. Run on an emulator or a physical Android device (API 24+).

## Admin Panel Integration

The Android app now expects the website admin panel and the app to share Firebase Authentication and Cloud Firestore.

### Firestore collections

`users/{uid}`

```json
{
  "fullName": "Amit Sharma",
  "industry": "MIDC Unit - Pune",
  "email": "amit@example.com",
  "organizationId": "midc-unit-pune",
  "role": "operator",
  "active": true,
  "createdAt": 1710000000000
}
```

`organizations/{organizationId}`

```json
{
  "name": "MIDC Unit - Pune",
  "plantLabel": "PLANT: MIDC UNIT - PUNE",
  "adminPanelEnabled": true,
  "primaryModuleName": "Bio WRP",
  "primaryModuleDescription": "Water Recycle Plant",
  "primaryModuleActive": true,
  "secondaryModuleName": "Bio ETP",
  "secondaryModuleDescription": "Effluent Treatment",
  "secondaryModuleActive": true,
  "updatedAt": 1710000000000
}
```

### How the website should connect

1. The website admin panel should log in with the same Firebase project as the app.
2. When the admin edits plant/module settings, the website should update the `organizations/{organizationId}` document.
3. When a user signs up in the app, the app stores their `organizationId` and creates a default organization document if it does not exist.
4. The app reads the organization document to show plant name, module names, module status, and admin-panel connection status.

### Recommended website stack

- Firebase Auth for admin login
- Firestore for shared app/admin data
- React, Next.js, or plain HTML admin panel UI
- Optional Cloud Functions if you want stronger admin-only validation or audit logs

## Live Website Connection

The Android app is now wired for the live Lab Digitiser API at `https://labdigitiser.nextin.space/api.php/`.

### Roles

- `Admin` should use the website dashboard
- `Member` should use the Android app
- Member submissions created in the app are sent to the shared backend so admins can review them on the website

### Android API-ready setup

- Base URL is stored in `BuildConfig.LABDIGITISER_BASE_URL`
- Retrofit + OkHttp network stack is configured
- Bearer-token session handling is prepared in `SessionStore` and `AuthInterceptor`
- Repository and model classes are aligned with the live JWT API for login, profile, plants, dashboard, locations, parameters, and readings

### Live endpoints verified

- `GET /health`
- `POST /auth/login`
- `GET /auth/me`
- `GET /plants`
- `GET /dashboard?plant_id={id}`
- `GET /plants/{id}/locations`
- `GET /plants/{id}/parameters`
- `POST /readings`
- `GET /readings/{id}`
- `DELETE /readings/{id}`

The live API currently uses JWT bearer authentication. The Android app now saves the returned token locally and uses it on protected requests.

## License

This project is private and intended for use by authorized personnel.
