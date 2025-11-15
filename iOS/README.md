# Cubemaster iOS

SwiftUI version of the Cubemaster app, matching the Android UI exactly.

## Project Structure

```
Cubemaster/
├── CubemasterApp.swift          # Main app entry point
├── Views/
│   ├── StartScreen.swift        # Start screen with title and button
│   ├── PermissionScreen.swift   # Camera permission request screen
│   └── ScanScreen.swift         # Camera scanning screen (placeholder)
├── Theme/
│   └── Colors.swift             # Color definitions matching Android
└── Info.plist                   # Camera permission description
```

## Setup Instructions (when you have Mac access)

### 1. Create Xcode Project

1. Open Xcode
2. File → New → Project
3. Choose "iOS" → "App"
4. Configure:
   - Product Name: `Cubemaster`
   - Team: Select your Apple ID (or create free account)
   - Organization Identifier: `com.cs407` (or your own)
   - Interface: `SwiftUI`
   - Language: `Swift`
   - Uncheck "Use Core Data" and "Include Tests" (optional)

### 2. Add Files to Project

1. Copy all files from this `iOS/Cubemaster/` folder into your Xcode project
2. In Xcode:
   - Right-click on the project in navigator
   - Select "Add Files to Cubemaster..."
   - Select all the Swift files and folders
   - Make sure "Copy items if needed" is checked
   - Make sure "Add to targets: Cubemaster" is checked

### 3. Configure Info.plist

The `Info.plist` file should already have the camera permission description. In modern Xcode projects, this might be in the project settings instead:

1. Select the project in Xcode
2. Select the "Cubemaster" target
3. Go to "Info" tab
4. Add key: `Privacy - Camera Usage Description`
5. Value: `This app needs camera access to scan the Rubik's cube`

### 4. Add Icon Asset (Optional)

1. Copy `rubik_icon.png` from `app/src/main/res/drawable/` to your project
2. In Xcode, open `Assets.xcassets`
3. Right-click → New Image Set
4. Name it `rubik_icon`
5. Drag the PNG file into the image set
6. Update `StartScreen.swift` to use `Image("rubik_icon")` instead of the SF Symbol placeholder

### 5. Connect iPhone and Run

1. Connect your iPhone via USB
2. Trust the computer on your iPhone (if prompted)
3. In Xcode, select your iPhone as the run destination (top toolbar)
4. If you see a signing error:
   - Select the project in Xcode
   - Go to "Signing & Capabilities"
   - Check "Automatically manage signing"
   - Select your Apple ID team
5. Click Run (▶️) or press Cmd+R
6. On your iPhone:
   - If prompted, go to Settings → General → VPN & Device Management
   - Trust the developer certificate
   - The app will install and launch

## Notes

- **Free Apple Developer Account**: The app will expire after 7 days. You'll need to re-sign and reinstall to continue testing.
- **Paid Account ($99/year)**: Apps last 1 year, and you can use TestFlight for distribution.
- **Icon**: Currently using SF Symbol `cube.fill` as placeholder. Replace with actual `rubik_icon.png` when available.

## Known Discrepancies

1. **Icon**: Using SF Symbol placeholder instead of `rubik_icon.png`. You'll need to add the asset to Xcode.
2. **Button Styling**: SwiftUI buttons have slightly different default styling than Material3 buttons, but the size and colors match.
3. **Typography**: Using system fonts which may render slightly differently than Android's default fonts, but sizes match.

## Next Steps

- [ ] Add `rubik_icon.png` to Assets.xcassets
- [ ] Test navigation flow on device
- [ ] Port `Cube.kt` business logic to Swift
- [ ] Implement camera functionality in ScanScreen

