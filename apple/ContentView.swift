import SwiftUI
import WebKit

struct ContentView: View {
    @State private var address = ""
    @State private var currentURL: URL? = nil

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                TextField("Enter a web address", text: $address)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .textFieldStyle(.roundedBorder)
                    .onSubmit { openAddress() }
                Button("Open") { openAddress() }
            }.padding(10)
            if let url = currentURL { JULEWebView(url: url) }
            else { VStack { Image("JULELogo").resizable().scaledToFit().frame(width: 180); Text("JULE").font(.system(size:64,weight:.bold)); Text("Open Anything.").foregroundStyle(.secondary) }.frame(maxWidth:.infinity,maxHeight:.infinity) }
        }
    }
    private func openAddress(){
        var v = address.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !v.isEmpty, !v.contains(" ") else { return }
        if !v.lowercased().hasPrefix("http://") && !v.lowercased().hasPrefix("https://") { v = "https://" + v }
        currentURL = URL(string: v)
    }
}

struct JULEWebView: UIViewRepresentable {
    let url: URL
    func makeCoordinator() -> Coordinator { Coordinator() }
    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        let view = WKWebView(frame: .zero, configuration: configuration)
        view.navigationDelegate = context.coordinator
        view.uiDelegate = context.coordinator
        view.load(URLRequest(url: url))
        return view
    }
    func updateUIView(_ view: WKWebView, context: Context) { if view.url != url { view.load(URLRequest(url:url)) } }
    final class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
            if let u = navigationAction.request.url, u.scheme == "http" || u.scheme == "https" { webView.load(URLRequest(url:u)) }
            return nil
        }
    }
}
