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
                    //self.removeView()
                })
            } else {
                //removeView()
            }
        }
    }
//
//    private func removeView() {
//          let parentView = viewController.view
//          parentView?.isUserInteractionEnabled = true
//
//          animationView?.removeFromSuperview()
//          animationViewContainer?.removeFromSuperview()
//
//          animationViewContainer = nil
//          animationView = nil
//          visible = false
//
//          sendCallback()
//      }
    
//    public func createView(location: String? = nil, remote: Bool? = nil, width: Int? = nil, height: Int? = nil, callbackId: String? = nil) -> Bool {
//        if !visible {
//                    self.callbackId = callbackId
//                    let parentView = viewController.view
//
//                    createAnimationViewContainer()
//                    do {
//                        try createAnimationView(location: location, remote: remote, width: width, height: height)
//                    } catch {
//                        processInvalidURLError(error: error)
//                    }
//
//                    animationViewContainer?.addSubview(animationView!)
//                    parentView?.addSubview(animationViewContainer!)
//
//                    let cancelOnTap = getConfigValue("LottieCancelOnTap".lowercased()) as? NSString ?? "false"
//                    if cancelOnTap.boolValue {
//                        let gesture = UITapGestureRecognizer(target: self, action: #selector(destroyView(_:)))
//                        animationViewContainer?.addGestureRecognizer(gesture)
//                    }
//
//                    let hideTimeout = Double(getConfigValue("LottieHideTimeout".lowercased()) as? String ?? "0")!
//                    if hideTimeout > 0 {
//                        delayWithSeconds(hideTimeout) {
//                            self.destroyView()
//                        }
//                    }
//
//                    playAnimation()
//                    visible = true
//                } else if callbackId != nil {
//                    let result = CDVPluginResult.init(status: CDVCommandStatus_ERROR, messageAs: LottieSplashScreenError.animationAlreadyPlaying.localizedDescription)
////                    commandDelegate.send(result, callbackId: callbackId)
//                }
//    }
    
//    private func createAnimationViewContainer() {
//        let parentView = //viewController.view
//        parentView?.isUserInteractionEnabled = false
//
//        animationViewContainer = UIView(frame: (parentView?.bounds)!)
//        animationViewContainer?.layer.zPosition = 1
//
//        let backgroundColor = getUIModeDependentPreference(basePreferenceName: "LottieBackgroundColor", defaultValue: "#ffffff")
//
//        animationViewContainer?.autoresizingMask = [
//            .flexibleWidth, .flexibleHeight, .flexibleTopMargin, .flexibleLeftMargin, .flexibleBottomMargin, .flexibleRightMargin
//        ]
//        animationViewContainer?.backgroundColor = UIColor(hex: backgroundColor)
//    }

    private func createAnimationView(location: String? = nil, remote: Bool? = nil, width: Int? = nil, height: Int? = nil) throws {
        var animationLocation = ""
        if location != nil {
            animationLocation = location!
        } else {
//            animationLocation = getUIModeDependentPreference(basePreferenceName: "LottieAnimationLocation")
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
        
        let lottieWidthPX =  Int(getConfig().getInt("LottieWidth".lowercased(), 200))
        let lottieHeightPX =  Int(getConfig().getInt("LottieHeight".lowercased(), 200))

        let useFullScreen = getConfig().getBoolean("LottieFullScreen", false)
        if useFullScreen {
            var autoresizingMask: UIView.AutoresizingMask = [
                .flexibleTopMargin, .flexibleLeftMargin, .flexibleBottomMargin, .flexibleRightMargin
            ]

            let portrait =
                UIApplication.shared.statusBarOrientation == UIInterfaceOrientation.portrait ||
                UIApplication.shared.statusBarOrientation == UIInterfaceOrientation.portraitUpsideDown
            autoresizingMask.insert(portrait ? .flexibleWidth : .flexibleHeight)

            animationView?.autoresizingMask = autoresizingMask
            animationWidth = fullScreenzSize.width
            animationHeight = fullScreenzSize.height
        } else {
            animationView?.autoresizingMask = [.flexibleTopMargin, .flexibleLeftMargin, .flexibleBottomMargin, .flexibleRightMargin]

            let useRelativeSize = getConfig().getBoolean("LottieRelativeSize", false);
            if useRelativeSize {
                animationWidth = fullScreenzSize.width *
                    (width != nil ?
                        CGFloat(width!) :
                        CGFloat(Float(getConfig().getString("LottieWidth".lowercased(), "0.2")!)!))
                animationHeight = fullScreenzSize.height *
                    (height != nil ?
                        CGFloat(height!) :
                        CGFloat(Float(getConfig().getString("LottieHeight".lowercased(), "0.2")!)!))
            } else {
               
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
//            self.webViewEngine.evaluateJavaScript("document.dispatchEvent(new Event('\(event)'))", completionHandler: nil)
            let hideAfterAnimationDone = self.getConfig().getBoolean("LottieHideAfterAnimationEnd", false)
            if hideAfterAnimationDone {
                self.destroyView()
            }
            self.animationEnded = true
        }
//        self.webViewEngine.evaluateJavaScript("document.dispatchEvent(new Event('lottieAnimationStart'))", completionHandler: nil)
        animationEnded = false
        sendCallback()
    }

    private func processInvalidURLError(error: Error) {
        if callbackId != nil {
            let result = CDVPluginResult.init(status: CDVCommandStatus_ERROR, messageAs: LottieSplashScreenError.invalidURL.localizedDescription)
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
            let result = CDVPluginResult.init(status: CDVCommandStatus_OK)
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

//    private func getUIModeDependentPreference(basePreferenceName: String, defaultValue: String = "") -> String {
//        var preferenceValue = ""
//        if #available(iOS 12.0, *) {
//            if viewController.traitCollection.userInterfaceStyle == .dark {
//                preferenceValue = getConfigValue((basePreferenceName + "Dark").lowercased()) as? String ?? defaultValue
//            } else {
//                preferenceValue = getConfigValue((basePreferenceName + "Light").lowercased()) as? String ?? defaultValue
//            }
//        }
//
//        if preferenceValue.isEmpty {
//            preferenceValue = getConfigValue(basePreferenceName.lowercased()) as? String ?? defaultValue
//        }
//        return preferenceValue
//    }

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
