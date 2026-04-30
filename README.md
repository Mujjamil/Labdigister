# LabDigitiser

LabDigitiser is a specialized Android application designed for monitoring water quality. It provides a secure and efficient platform for member users to capture plant readings that are managed through the website admin panel.

## Features

- **Secure Authentication**: Website-backed member login with JWT session handling.
- **Dashboard**: Real-time overview of key metrics like pH, TDS, BOD, and COD.
- **Reports**: Detailed data visualization and historical report tracking.
- **Data Entries**: Easy-to-use forms for recording new water quality measurements.
- **Export Options**: Export data to various formats including PDF and Excel.
- **Website-Connected Backend**: Member actions in the app are sent to the shared Lab Digitiser backend so admins can review them on the website.

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

## Live Website Connection

The Android app is now wired for the live Lab Digitiser API at `https://labdigitiser.nextin.space/api.php/`.

### Roles

- `Admin` should use the website dashboard
- `Member` should use the Android app
- Member submissions created in the app are sent to the shared backend so admins can review them on the website
- Sign up is managed by the website/admin side, not from the Android app

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
