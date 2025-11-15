import SwiftUI

@main
struct CubemasterApp: App {
    @StateObject private var permissionManager = PermissionManager()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(permissionManager)
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var permissionManager: PermissionManager
    
    var body: some View {
        NavigationStack {
            StartScreen()
                .environmentObject(permissionManager)
        }
    }
}

