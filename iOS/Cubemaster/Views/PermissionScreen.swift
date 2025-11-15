import SwiftUI
import AVFoundation

class PermissionManager: ObservableObject {
    @Published var permissionGranted: Bool = false
    @Published var showPermissionDeniedMessage: Bool = false
    
    init() {
        checkPermissionStatus()
    }
    
    func checkPermissionStatus() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        permissionGranted = status == .authorized
        showPermissionDeniedMessage = status == .denied
    }
    
    func requestPermission() {
        AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
            DispatchQueue.main.async {
                self?.permissionGranted = granted
                if !granted {
                    // Check if we should show the denied message
                    // On iOS, if status is denied, user must go to settings
                    let status = AVCaptureDevice.authorizationStatus(for: .video)
                    self?.showPermissionDeniedMessage = status == .denied
                }
            }
        }
    }
    
    func openSettings() {
        if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(settingsUrl)
        }
    }
}

struct PermissionScreen: View {
    @EnvironmentObject var permissionManager: PermissionManager
    @Environment(\.dismiss) var dismiss
    @State private var navigationPath = NavigationPath()
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            ZStack {
                // Gradient background matching Android
                LinearGradient(
                    colors: [Colors.lightOrange, Colors.darkOrange],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                // Main content column (16dp padding, centered)
                VStack(spacing: 0) {
                    Spacer()
                    
                    Text("Camera Permission Required")
                        .font(.title2) // headlineMedium equivalent
                        .fontWeight(.semibold)
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                    
                    Spacer()
                        .frame(height: 16)
                    
                    Text(permissionText)
                        .font(.body) // bodyLarge equivalent
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 16)
                    
                    Spacer()
                        .frame(height: 32)
                    
                    // Conditional button based on permission state
                    if permissionManager.showPermissionDeniedMessage {
                        Button(action: {
                            permissionManager.openSettings()
                        }) {
                            Text("Open Settings")
                                .font(.headline)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .cornerRadius(12)
                        }
                        .padding(.horizontal, 16)
                    } else {
                        Button(action: {
                            permissionManager.requestPermission()
                        }) {
                            Text("Grant Permission")
                                .font(.headline)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(permissionManager.permissionGranted ? Color.gray : Color.blue)
                                .cornerRadius(12)
                        }
                        .disabled(permissionManager.permissionGranted)
                        .padding(.horizontal, 16)
                    }
                    
                    Spacer()
                }
                .padding(16)
                
                // Bottom buttons overlay
                VStack {
                    Spacer()
                    HStack {
                        // Back button (bottom-left, 16dp padding)
                        Button(action: {
                            dismiss()
                        }) {
                            Text("Back")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding()
                                .background(Color.blue)
                                .cornerRadius(12)
                        }
                        .padding(16)
                        
                        Spacer()
                        
                        // Next button (bottom-right, 16dp padding, only when permission granted)
                        if permissionManager.permissionGranted {
                            Button(action: {
                                navigationPath.append("scan")
                            }) {
                                Text("Next")
                                    .font(.headline)
                                    .foregroundColor(.white)
                                    .padding()
                                    .background(Color.blue)
                                    .cornerRadius(12)
                            }
                            .padding(16)
                        }
                    }
                }
            }
            .navigationDestination(for: String.self) { destination in
                if destination == "scan" {
                    ScanScreen()
                }
            }
        }
    }
    
    private var permissionText: String {
        if permissionManager.permissionGranted {
            return "Permission Granted!"
        } else if permissionManager.showPermissionDeniedMessage {
            return "Camera permission is required to scan the cube. Please enable it in the app settings."
        } else {
            return "This app needs camera access to scan your Rubik's Cube. Please grant the permission to continue."
        }
    }
}

