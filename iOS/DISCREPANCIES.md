# Known Discrepancies and Notes

This document tracks differences between the Android (Jetpack Compose) and iOS (SwiftUI) implementations, and any issues that need attention.

## UI Layout Differences

### 1. Icon Asset
- **Android**: Uses `R.drawable.rubik_icon` (PNG file)
- **iOS**: Currently uses SF Symbol `cube.fill` as placeholder
- **Action Required**: 
  - Copy `app/src/main/res/drawable/rubik_icon.png` to Xcode Assets.xcassets
  - Update `StartScreen.swift` line 28-31 to use `Image("rubik_icon")` instead of SF Symbol

### 2. Button Styling
- **Android**: Material3 Button with default Material styling
- **iOS**: SwiftUI Button with custom styling (blue background, white text, 12pt corner radius)
- **Status**: Functionally equivalent, but may have slight visual differences in default button appearance
- **Note**: Size (200x80 points) and colors match exactly

### 3. Typography
- **Android**: Uses Material3 Typography with system default font
- **iOS**: Uses system font (San Francisco)
- **Status**: Fonts will render slightly differently, but sizes match:
  - Title: 48pt bold
  - Subtitle: 32pt bold  
  - Body: 16pt (system default)
  - Button: 24pt semibold

### 4. Shadow Effects
- **Android**: `shadow(elevation = 16.dp)` on circle
- **iOS**: `shadow(radius: 16, x: 0, y: 8)` on circle
- **Status**: Close approximation, but Material elevation shadows are more complex than simple drop shadows
- **Note**: Visual difference should be minimal

### 5. Text Shadow (Title)
- **Android**: Two Text composables layered with 4dp offset for shadow effect
- **iOS**: Two Text views layered with 2pt offset (4dp ≈ 2-4pt depending on device)
- **Status**: Matches the approach, but offset may need fine-tuning on actual device

## Platform-Specific Differences

### 1. Navigation
- **Android**: Uses Jetpack Navigation Compose with string-based routes
- **iOS**: Uses SwiftUI NavigationStack with string-based routes
- **Status**: Functionally equivalent, both support the same navigation pattern

### 2. Permissions
- **Android**: Uses Activity Result Contract for runtime permissions
- **iOS**: Uses AVFoundation with Info.plist usage description
- **Status**: Different APIs but same user experience
- **Note**: iOS permissions are requested once and remembered; Android requires runtime checks

### 3. Gradient Background
- **Android**: `Brush.verticalGradient` with LightOrange → DarkOrange
- **iOS**: `LinearGradient` with same colors, top to bottom
- **Status**: Should render identically

## Layout Verification Needed

When testing on device, verify:
1. ✅ Circle icon is centered and visible behind text
2. ✅ Title "CUBEMASTER" has visible blue shadow effect
3. ✅ Button is positioned 64pt from bottom
4. ✅ Permission screen buttons are positioned correctly (back bottom-left, next bottom-right)
5. ✅ Scan screen has two equal-height boxes (top for camera, bottom for controls)
6. ✅ All spacing matches (100pt top spacer, 32pt between elements, 16pt padding)

## Color Verification

All colors match Android hex values:
- ✅ DarkYellow: `#FFB300` (0xFFFFB300)
- ✅ Blue: `#0000FF` (0xFF0000FF)  
- ✅ LightOrange: `#E6A788` (0xFFE6A788)
- ✅ DarkOrange: `#8C2B0A` (0xFF8C2B0A)

## Next Steps

1. Test on actual iPhone device to verify layout
2. Add `rubik_icon.png` to Assets.xcassets
3. Fine-tune any spacing/sizing discrepancies found during testing
4. Port business logic (`Cube.kt` → Swift)
5. Implement camera functionality in ScanScreen

