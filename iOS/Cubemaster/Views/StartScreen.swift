import SwiftUI

struct StartScreen: View {
    @EnvironmentObject var permissionManager: PermissionManager
    @State private var navigationPath = NavigationPath()
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            ZStack {
                // Gradient background matching Android: LightOrange to DarkOrange
                LinearGradient(
                    colors: [Colors.lightOrange, Colors.darkOrange],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                // Yellow circle with icon (200dp = ~200 points, icon 150dp = ~150 points)
                ZStack {
                    // Circle positioned in center
                    Circle()
                        .fill(Colors.darkYellow)
                        .frame(width: 200, height: 200)
                        .shadow(color: .black.opacity(0.3), radius: 16, x: 0, y: 8)
                        .overlay {
                            // Icon placeholder - you'll need to add rubik_icon.png to Assets.xcassets
                            // For now using SF Symbol as placeholder
                            Image(systemName: "cube.fill")
                                .font(.system(size: 100))
                                .foregroundColor(.white)
                            // TODO: Replace with: Image("rubik_icon").resizable().scaledToFit().frame(width: 150, height: 150)
                        }
                }
                
                // Column layout matching Android structure
                VStack(spacing: 0) {
                    // Spacer matching 100dp from top
                    Spacer()
                        .frame(height: 100)
                    
                    // Title with shadow effect (4dp offset)
                    ZStack {
                        // Shadow layer (blue, offset 4dp = ~4 points)
                        Text("CUBEMASTER")
                            .font(.system(size: 48, weight: .bold))
                            .foregroundColor(Colors.blue)
                            .offset(x: 2, y: 2) // 4dp offset approximated
                        
                        // Main text (white)
                        Text("CUBEMASTER")
                            .font(.system(size: 48, weight: .bold))
                            .foregroundColor(.white)
                    }
                    
                    // Spacer matching 32dp
                    Spacer()
                        .frame(height: 32)
                    
                    // "Happy Solving!" text
                    Text("Happy Solving!")
                        .font(.system(size: 32, weight: .bold))
                        .foregroundColor(.white)
                    
                    // Flexible spacer to push button to bottom
                    Spacer()
                    
                    // Start button (200dp x 80dp, 64dp padding from bottom)
                    Button(action: {
                        navigationPath.append("permission")
                    }) {
                        Text("Start")
                            .font(.system(size: 24, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(width: 200, height: 80)
                            .background(Color.blue)
                            .cornerRadius(12)
                    }
                    .padding(.bottom, 64)
                }
            }
            .navigationDestination(for: String.self) { destination in
                if destination == "permission" {
                    PermissionScreen()
                        .environmentObject(permissionManager)
                } else if destination == "scan" {
                    ScanScreen()
                }
            }
        }
    }
}

