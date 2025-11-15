# Mac Setup Guide for Cubemaster iOS

This guide walks you through setting up the iOS project on a Mac and deploying it to your iPhone for testing.

## Prerequisites

- Mac computer with macOS (required for iOS development)
- iPhone for testing
- USB cable to connect iPhone to Mac
- Apple ID (free account works, but apps expire after 7 days)

## Step 1: Install Xcode

1. Open the **App Store** on your Mac
2. Search for "Xcode"
3. Click **Get** or **Install** (it's free, but large - ~15GB, may take 30+ minutes)
4. Wait for installation to complete
5. Open Xcode from Applications
6. Accept the license agreement if prompted
7. Xcode may install additional components - wait for this to complete

**Note**: First-time Xcode setup can take 30-60 minutes depending on your internet speed.

## Step 2: Sign In with Apple ID

1. Open Xcode
2. Go to **Xcode → Settings** (or **Preferences** on older macOS)
3. Click the **Accounts** tab
4. Click the **+** button in bottom left
5. Select **Apple ID**
6. Sign in with your Apple ID
   - If you don't have one, click "Create Apple ID" and follow the prompts
   - Free accounts work fine for development

## Step 3: Create New Xcode Project

1. In Xcode, go to **File → New → Project** (or press `Cmd+Shift+N`)
2. Select **iOS** at the top
3. Choose **App** template
4. Click **Next**
5. Configure the project:
   - **Product Name**: `Cubemaster`
   - **Team**: Select your Apple ID from the dropdown
     - If you see "Add an account...", click it and sign in
   - **Organization Identifier**: `com.cs407` (or use your own, e.g., `com.yourname`)
   - **Bundle Identifier**: Will auto-fill as `com.cs407.Cubemaster`
   - **Interface**: Select **SwiftUI**
   - **Language**: Select **Swift**
   - **Storage**: Leave as default (or uncheck "Use Core Data" if you see it)
   - **Include Tests**: Optional (you can uncheck if you want)
6. Click **Next**
7. Choose a location to save the project (e.g., Desktop or Documents)
8. Click **Create**

Xcode will create a new project with a basic "Hello, World!" SwiftUI app.

## Step 4: Add Project Files

You need to add the Swift files from the `iOS/Cubemaster/` folder to your Xcode project.

### Option A: Drag and Drop (Easiest)

1. In Finder, navigate to the `iOS/Cubemaster/` folder in your project
2. In Xcode, in the left sidebar (Project Navigator), right-click on the `Cubemaster` folder (the blue project icon)
3. Select **Add Files to "Cubemaster"...**
4. Navigate to and select the `iOS/Cubemaster/` folder
5. **Important**: Make sure these options are checked:
   - ✅ **Copy items if needed**
   - ✅ **Create groups** (not "Create folder references")
   - ✅ **Add to targets: Cubemaster**
6. Click **Add**

### Option B: Manual File Addition

1. In Xcode Project Navigator, right-click on `Cubemaster` folder
2. Select **New Group** and name it `Views`
3. Right-click on `Cubemaster` again, create another group called `Theme`
4. For each file:
   - Right-click the appropriate group
   - Select **Add Files to "Cubemaster"...**
   - Select the file
   - Check "Copy items if needed" and "Add to targets: Cubemaster"
   - Click Add

**Files to add:**
- `CubemasterApp.swift` → Root `Cubemaster` folder
- `Views/StartScreen.swift` → `Views` group
- `Views/PermissionScreen.swift` → `Views` group
- `Views/ScanScreen.swift` → `Views` group
- `Theme/Colors.swift` → `Theme` group
- `Info.plist` → Root `Cubemaster` folder (may need special handling - see Step 5)

## Step 5: Configure Info.plist

Modern Xcode projects (iOS 14+) may not use a separate `Info.plist` file. Instead, settings are in the project settings.

### If Info.plist exists in your project:

1. Select `Info.plist` in Project Navigator
2. Verify it contains the camera permission key:
   ```xml
   <key>NSCameraUsageDescription</key>
   <string>This app needs camera access to scan the Rubik's cube</string>
   ```

### If using project settings (more common):

1. Select the **Cubemaster** project (blue icon) in Project Navigator
2. Select the **Cubemaster** target under "TARGETS"
3. Click the **Info** tab
4. Under "Custom iOS Target Properties", look for "Privacy - Camera Usage Description"
   - If it exists, verify the value is: `This app needs camera access to scan the Rubik's cube`
   - If it doesn't exist:
     - Click the **+** button
     - Type: `Privacy - Camera Usage Description` (or select it from the dropdown)
     - Set the value to: `This app needs camera access to scan the Rubik's cube`

## Step 6: Replace Default App File

The Xcode template creates a default `ContentView.swift` and `CubemasterApp.swift`. You need to replace the default `CubemasterApp.swift` with yours.

1. In Project Navigator, find `CubemasterApp.swift`
2. Select it
3. Replace the entire contents with the code from `iOS/Cubemaster/CubemasterApp.swift`
4. If there's a default `ContentView.swift`, you can delete it (right-click → Delete → Move to Trash)

## Step 7: Add Icon Asset (Optional)

1. In Finder, navigate to `app/src/main/res/drawable/rubik_icon.png` in your Android project
2. Copy the file
3. In Xcode, open `Assets.xcassets` in Project Navigator
4. Right-click in the left sidebar of Assets
5. Select **New Image Set**
6. Name it `rubik_icon`
7. Drag `rubik_icon.png` into the "1x" slot (or "Universal" if available)
8. Open `Views/StartScreen.swift`
9. Find the line with `Image(systemName: "cube.fill")`
10. Replace it with:
    ```swift
    Image("rubik_icon")
        .resizable()
        .scaledToFit()
        .frame(width: 150, height: 150)
    ```

## Step 8: Build and Test in Simulator

Before deploying to your iPhone, test in the iOS Simulator:

1. At the top of Xcode, next to the Run button, click the device selector
2. Select an iPhone simulator (e.g., "iPhone 15" or "iPhone 15 Pro")
3. Click the **Run** button (▶️) or press `Cmd+R`
4. Wait for the app to build and launch in the simulator
5. Test the navigation:
   - Tap "Start" button
   - Test permission screen
   - Navigate back

If you see any build errors, see the Troubleshooting section below.

## Step 9: Connect iPhone and Deploy

### 9.1: Prepare iPhone

1. Connect your iPhone to the Mac via USB cable
2. On your iPhone, if prompted, tap **Trust This Computer**
3. Enter your iPhone passcode if requested
4. On Mac, if Finder opens, you can close it

### 9.2: Configure Signing in Xcode

1. In Xcode, select the **Cubemaster** project (blue icon) in Project Navigator
2. Select the **Cubemaster** target under "TARGETS"
3. Click the **Signing & Capabilities** tab
4. Check **Automatically manage signing**
5. Select your **Team** from the dropdown (your Apple ID)
6. Xcode will automatically create a provisioning profile

**If you see signing errors:**
- Make sure you're signed in with your Apple ID (Step 2)
- Try unchecking and rechecking "Automatically manage signing"
- Click "Try Again" if Xcode shows a retry button

### 9.3: Select iPhone as Target

1. At the top of Xcode, click the device selector (next to Run button)
2. Your iPhone should appear under "iOS Device" or with its name
3. Select your iPhone

### 9.4: Build and Run

1. Click the **Run** button (▶️) or press `Cmd+R`
2. Xcode will build the app (first build may take 1-2 minutes)
3. The app will install on your iPhone
4. On your iPhone, you may see: **"Untrusted Developer"**
   - Go to **Settings → General → VPN & Device Management** (or **Device Management**)
   - Tap your Apple ID email
   - Tap **Trust "[Your Email]"**
   - Tap **Trust** in the confirmation dialog
5. Return to the home screen and tap the Cubemaster app icon
6. The app should launch!

## Step 10: Verify App Works

Test the following on your iPhone:

1. ✅ App launches and shows Start screen
2. ✅ Gradient background displays correctly
3. ✅ "CUBEMASTER" title is visible with shadow effect
4. ✅ "Start" button is visible and tappable
5. ✅ Tapping "Start" navigates to Permission screen
6. ✅ Permission screen displays correctly
7. ✅ Tapping "Grant Permission" requests camera permission
8. ✅ After granting permission, "Next" button appears
9. ✅ Tapping "Next" navigates to Scan screen
10. ✅ "Back" buttons work on all screens

## Troubleshooting

### Build Errors

**Error: "No such module 'SwiftUI'"**
- Make sure you selected "SwiftUI" as the interface when creating the project
- Check that your deployment target is iOS 13.0 or higher (Project → Target → General → Deployment Info)

**Error: "Cannot find 'PermissionManager' in scope"**
- Make sure `PermissionScreen.swift` was added to the project
- Check that all files are added to the "Cubemaster" target (select file → File Inspector → Target Membership)

**Error: "Cannot find 'Colors' in scope"**
- Make sure `Theme/Colors.swift` was added to the project
- Verify the file is in the correct group/folder

**Error: "Use of unresolved identifier"**
- Clean build folder: **Product → Clean Build Folder** (or `Cmd+Shift+K`)
- Rebuild: **Product → Build** (or `Cmd+B`)

### Signing Errors

**Error: "No signing certificate found"**
- Make sure you're signed in with Apple ID (Xcode → Settings → Accounts)
- Check "Automatically manage signing" is enabled
- Try selecting your team again from the dropdown

**Error: "Provisioning profile doesn't match"**
- Uncheck and recheck "Automatically manage signing"
- Let Xcode regenerate the provisioning profile

### Device Connection Issues

**iPhone doesn't appear in device list**
- Unplug and replug the USB cable
- Trust the computer on iPhone (Settings → General → Reset → Reset Location & Privacy)
- Try a different USB cable or port
- Restart Xcode

**"Untrusted Developer" on iPhone**
- Go to Settings → General → VPN & Device Management
- Trust your developer certificate
- See Step 9.4 above

### App Crashes on Launch

- Check Xcode console for error messages (bottom panel)
- Verify all files were added to the target
- Make sure Info.plist camera permission is configured
- Try cleaning and rebuilding

### App Expires After 7 Days

This is normal with a free Apple Developer account. To continue testing:
1. Reconnect iPhone to Mac
2. Open the project in Xcode
3. Select your iPhone as target
4. Click Run again
5. The app will be re-signed and work for another 7 days

To avoid this, you can purchase a paid Apple Developer account ($99/year).

## Next Steps

Once the app is running on your iPhone:

1. ✅ Verify UI matches Android version
2. ✅ Test all navigation flows
3. ✅ Test camera permission flow
4. ⏭️ Port business logic (Cube.kt) - see KMP refactoring guide
5. ⏭️ Implement camera functionality in ScanScreen

## Additional Resources

- [Apple Developer Documentation](https://developer.apple.com/documentation/)
- [SwiftUI Tutorials](https://developer.apple.com/tutorials/swiftui)
- [Xcode Help](https://help.apple.com/xcode/)

## Quick Reference

**Keyboard Shortcuts:**
- `Cmd+R` - Build and Run
- `Cmd+B` - Build only
- `Cmd+Shift+K` - Clean Build Folder
- `Cmd+.` - Stop running app

**Important Paths:**
- Project files: `iOS/Cubemaster/`
- Icon asset: `app/src/main/res/drawable/rubik_icon.png`
- Android project: Root directory

---

**Last Updated**: [Date will be filled when guide is finalized]
**Tested on**: Xcode 15+, iOS 17+

