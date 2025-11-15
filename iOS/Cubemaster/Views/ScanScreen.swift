import SwiftUI

struct ScanScreen: View {
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        ZStack {
            // Gradient background matching Android
            LinearGradient(
                colors: [Colors.lightOrange, Colors.darkOrange],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            // Column with two equal boxes (weight 1f each)
            VStack(spacing: 0) {
                // Top half - Camera view (weight 1f, 32dp padding, 4dp border in LightOrange)
                ZStack {
                    RoundedRectangle(cornerRadius: 0)
                        .fill(Color.clear)
                        .overlay {
                            RoundedRectangle(cornerRadius: 0)
                                .stroke(Colors.lightOrange, lineWidth: 4)
                        }
                }
                .frame(maxWidth: .infinity)
                .frame(maxHeight: .infinity)
                .padding(32)
                
                // Bottom half - Controls (weight 1f, 32dp padding, LightOrange background)
                ZStack {
                    Colors.lightOrange
                }
                .frame(maxWidth: .infinity)
                .frame(maxHeight: .infinity)
                .padding(32)
            }
            
            // Back button (bottom-left, 16dp padding)
            VStack {
                Spacer()
                HStack {
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
                }
            }
        }
    }
}
