import Foundation
import Capacitor
import Lottie

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(LottieSplashscreenPlugin)
public class LottieSplashscreenPlugin: CAPPlugin {
    private let implementation = LottieSplashscreen()
    
    var animationView: LottieAnimationView?
    var animationViewContainer: UIView?
    var visible = false
    var animationEnded = false
    var callbackId: String?
    
    override public func load() {
        createObservers()
        createView()
    }
    

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
    
    @objc func pageDidLoad() {
        let autoHide = getConfig().getBoolean("LottieAutoHideSplashScreen".lowercased(), false)
        if autoHide {
            destroyView()
        }
    }

    private func delayWithSeconds(_ seconds: Double, completion: @escaping () -> Void) {
        DispatchQueue.main.asyncAfter(deadline: .now() + seconds) {
            completion()
        }
    }
    
    @objc private func destroyView(_: UITapGestureRecognizer? = nil) {
        if visible {
            let fadeOutDuation = Double(getConfig().getString("LottieFadeOutDuration".lowercased(), "0")!)!
            if fadeOutDuation > 0 {
                UIView.animate(withDuration: fadeOutDuation, animations: {
                    self.animationView?.alpha = 0.0
                }, completion: { _ in
                    self.removeView()
                })
            } else {
                removeView()
            }
        }
    }

    private func removeView() {
          let parentView = UIApplication.shared.windows.filter {$0.isKeyWindow}.first!
          parentView.isUserInteractionEnabled = true

          animationView?.removeFromSuperview()
          animationViewContainer?.removeFromSuperview()

          animationViewContainer = nil
          animationView = nil
          visible = false

          sendCallback()
      }
    
    public func createView(location: String? = nil, remote: Bool? = nil, width: Int? = nil, height: Int? = nil, callbackId: String? = nil) {
        if !visible {
                    self.callbackId = callbackId
                    let parentView = UIApplication.shared.windows.filter {$0.isKeyWindow}.first!

                    createAnimationViewContainer()
                    do {
                        try createAnimationView(location: location, remote: remote, width: width, height: height)
                    } catch {
                        processInvalidURLError(error: error)
                    }

                    animationViewContainer?.addSubview(animationView!)
                    parentView.addSubview(animationViewContainer!)

            let cancelOnTap = getConfig().getBoolean("LottieCancelOnTap", false)
                    if cancelOnTap {
                        let gesture = UITapGestureRecognizer(target: self, action: #selector(destroyView(_:)))
                        animationViewContainer?.addGestureRecognizer(gesture)
                    }

            let hideTimeout = Double(getConfig().getString("LottieHideTimeout".lowercased(), "0")!)!
                    if hideTimeout > 0 {
                        delayWithSeconds(hideTimeout) {
                            self.destroyView()
                        }
                    }

                    playAnimation()
                    visible = true
                } else if callbackId != nil {
                    _ = CAPPluginCallError.init(message:LottieSplashScreenError.animationAlreadyPlaying.localizedDescription, code:nil, error:nil, data:nil)
                    
                }
    }
    
    private func createAnimationViewContainer() {
        let parentView = UIApplication.shared.windows.filter {$0.isKeyWindow}.first!
        parentView.isUserInteractionEnabled = false

        animationViewContainer = UIView(frame: (parentView.bounds))
        animationViewContainer?.layer.zPosition = 1

        let backgroundColor = getUIModeDependentPreference(basePreferenceName: "LottieBackgroundColor", defaultValue: "#ffffff")

        animationViewContainer?.autoresizingMask = [
            .flexibleWidth, .flexibleHeight, .flexibleTopMargin, .flexibleLeftMargin, .flexibleBottomMargin, .flexibleRightMargin
        ]
        animationViewContainer?.backgroundColor = UIColor(hex: backgroundColor)
    }

    private func createAnimationView(location: String? = nil, remote: Bool? = nil, width: Int? = nil, height: Int? = nil) throws {
        var animationLocation = ""
        if location != nil {
            animationLocation = location!
        } else {
            animationLocation = getUIModeDependentPreference(basePreferenceName: "LottieAnimationLocation")
        }

        if isRemote(remote: remote) {
            let cacheDisabled = getConfig().getBoolean("LottieCacheDisabled", false)
            guard let url = URL(string: animationLocation) else { throw LottieSplashScreenError.invalidURL }
            animationView = LottieAnimationView(url: url, closure: { error in
                if error == nil {
                    self.playAnimation()
                } else {
                    self.destroyView()
                    self.processInvalidURLError(error: error!)
                }
            }, animationCache: cacheDisabled ? nil : DefaultAnimationCache.sharedCache)
        } else {
            animationLocation = Bundle.main.bundleURL.appendingPathComponent(animationLocation).path
            animationView = LottieAnimationView(filePath: animationLocation)
        }

        calculateAnimationSize(width: width, height: height)

        let loop = getConfig().getBoolean("LottieLiipAnimation", false)
        if loop {
            animationView?.loopMode = .loop
        }
        animationView?.contentMode = .scaleAspectFit
        animationView?.animationSpeed = 1
        animationView?.autoresizesSubviews = true
        animationView?.backgroundBehavior = .pauseAndRestore
    }

    private func calculateAnimationSize(width: Int? = nil, height: Int? = nil) {
        let fullScreenzSize = UIScreen.main.bounds
        var animationWidth: CGFloat
        var animationHeight: CGFloat
        
        
        let useFullScreen = getConfig().getBoolean("LottieFullScreen", false)
        if useFullScreen {
            let appInterfaceOrientation = UIApplication.shared.windows.first?.windowScene?.interfaceOrientation;
            var autoresizingMask: UIView.AutoresizingMask = [
                .flexibleTopMargin, .flexibleLeftMargin, .flexibleBottomMargin, .flexibleRightMargin
            ]

            let portrait = appInterfaceOrientation?.isPortrait != nil && appInterfaceOrientation!.isPortrait ||
            appInterfaceOrientation! == UIInterfaceOrientation.portraitUpsideDown
            autoresizingMask.insert(portrait ? .flexibleWidth : .flexibleHeight)

            animationView?.autoresizingMask = autoresizingMask
            animationWidth = fullScreenzSize.width
            animationHeight = fullScreenzSize.height
        } else {
            let lottieWidthRelative = getConfig().getString("LottieWidth".lowercased(), "0.2")
            let lottieHeightRelative = getConfig().getString("LottieHeight".lowercased(), "0.2")
            animationView?.autoresizingMask = [.flexibleTopMargin, .flexibleLeftMargin, .flexibleBottomMargin, .flexibleRightMargin]

            let useRelativeSize = getConfig().getBoolean("LottieRelativeSize", false);
            if useRelativeSize {
                animationWidth = fullScreenzSize.width *
                    (width != nil ?
                        CGFloat(width!) :
                        CGFloat(Float(lottieWidthRelative!)!))
                animationHeight = fullScreenzSize.height *
                    (height != nil ?
                        CGFloat(height!) :
                        CGFloat(Float(lottieHeightRelative!)!))
            } else {
                let lottieWidthPX =  Int(getConfig().getInt("LottieWidth".lowercased(), 200))
                let lottieHeightPX =  Int(getConfig().getInt("LottieHeight".lowercased(), 200))
                animationWidth = CGFloat(width != nil ?
                    width! : lottieWidthPX)
                animationHeight = CGFloat(height != nil
                    ? height! : lottieHeightPX)
            }
        }
        animationView?.frame = CGRect(x: 0, y: 0, width: animationWidth, height: animationHeight)
        animationView?.center = CGPoint(x: UIScreen.main.bounds.midX, y: UIScreen.main.bounds.midY)
    }

    private func playAnimation() {
        animationView?.play { finished in
            var event = "lottieAnimationEnd"
            if !finished {
                event =  "lottieAnimationCancel"
            }
            self.webView!.evaluateJavaScript("document.dispatchEvent(new Event('\(event)'))")
            let hideAfterAnimationDone = self.getConfig().getBoolean("LottieHideAfterAnimationEnd", false)
            if hideAfterAnimationDone {
                self.destroyView()
            }
            self.animationEnded = true
        }
        self.webView!.evaluateJavaScript("document.dispatchEvent(new Event('lottieAnimationStart'))")
        animationEnded = false
        sendCallback()
    }

    private func processInvalidURLError(error: Error) {
        if callbackId != nil {
            _ = CDVPluginResult.init(status: CDVCommandStatus_ERROR, messageAs: LottieSplashScreenError.invalidURL.localizedDescription)
//            commandDelegate.send(result, callbackId: callbackId)
        } else {
            NSLog("Unexpected error: \(error.localizedDescription)")
        }
    }

    private func isRemote(remote: Bool?) -> Bool {
        var useRemote: Bool
        if remote != nil {
            useRemote = remote!
        } else {
            useRemote = getConfig().getBoolean("LottieRemoteEnabled", false)
        }
        return useRemote
    }

    private func sendCallback() {
        if callbackId != nil {
            _ = CDVPluginResult.init(status: CDVCommandStatus_OK)
//            commandDelegate.send(result, callbackId: callbackId)
            callbackId = nil
        }
    }

    private func createObservers() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(pageDidLoad),
            name: NSNotification.Name.CDVPageDidLoad,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(deviceOrientationChanged),
            name: UIDevice.orientationDidChangeNotification,
            object: nil
        )
    }

    private func getUIModeDependentPreference(basePreferenceName: String, defaultValue: String = "") -> String {
        var preferenceValue = ""
        if #available(iOS 12.0, *) {
            if UITraitCollection.current.userInterfaceStyle == .dark {
                preferenceValue = getConfig().getString(basePreferenceName + "Dark", defaultValue)!
            } else {
                preferenceValue = getConfig().getString(basePreferenceName + "Light", defaultValue)!
            }
        }

        if preferenceValue.isEmpty {
            preferenceValue = getConfig().getString(basePreferenceName.lowercased(), defaultValue)!
       }
        return preferenceValue
    }

    @objc private func deviceOrientationChanged() {
        animationView?.center = CGPoint(x: UIScreen.main.bounds.midX, y: UIScreen.main.bounds.midY)
    }
}

enum LottieSplashScreenError: Error {
    case animationAlreadyPlaying
    case invalidURL
}

extension LottieSplashScreenError: LocalizedError {
    public var errorDescription: String? {
        switch self {
        case .animationAlreadyPlaying:
            return NSLocalizedString("An animation is already playing, please first hide the current one", comment: "")
        case .invalidURL:
            return NSLocalizedString("The provided URL is invalid", comment: "")
        }
    }
}


extension UIColor {
    convenience init(hex: String?) {
        let hexString = hex ?? "FFFFFFFF"
        var colorString: String = hexString.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

        if colorString.hasPrefix("#") {
            colorString.remove(at: colorString.startIndex)
        }

        if colorString.count != 6, colorString.count != 8 {
            colorString = "FFFFFF"
        }

        let hasAlpha = colorString.count > 7
        if !hasAlpha {
            colorString += "FF"
        }

        var rgbValue: UInt64 = 0
        Scanner(string: colorString).scanHexInt64(&rgbValue)

        self.init(
            red: CGFloat((rgbValue & 0xFF00_0000) >> 24) / 255.0,
            green: CGFloat((rgbValue & 0x00FF_0000) >> 16) / 255.0,
            blue: CGFloat((rgbValue & 0x0000_FF00) >> 8) / 255.0,
            alpha: CGFloat(rgbValue & 0x0000_00FF) / 255.0
        )
    }
}

