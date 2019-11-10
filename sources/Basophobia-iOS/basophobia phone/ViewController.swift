//
//  ViewController.swift
//  Basophobia Phone
//
//  Created by Lim Wern Jie on 09/09/2017.
//  Copyright © 2017 Lim Wern Jie. All rights reserved.
//

import UIKit
import AVFoundation
import CoreMotion

class ViewController: UIViewController {
    
    let motionManager = CMMotionManager()
    
    var audioPlayerScream: AVAudioPlayer?
    var audioPlayerKnock: AVAudioPlayer?
    var audioBackgrounding: AVAudioPlayer?
    var speechSynthesis: AVSpeechSynthesizer?
    
    var wasPlaying = false
    
    var dropDate: Date?
    var endDropDate: Date?
    var lastBump: Date?
    var bumps: [Corner] = []
    
    enum Corner: Int {
        case topRight = 1
        case bottomRight = 2
        case bottomLeft = 3
        case topLeft = 4
        
        public var description: String {
            switch self {
                case .topRight: return "top right"
                case .topLeft: return "top left"
                case .bottomRight: return "bottom right"
                case .bottomLeft: return "bottom left"
            }
        }
    }
    
    var distanceImpact : Double? {
        guard let dropDate = dropDate,
            let endDropDate = endDropDate
            else {
                return nil
        }
        
        let time = dropDate.timeIntervalSince(endDropDate)
        
        let distance = 1/2 * 9.80665 * (time*time)
        return distance
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        try? AVAudioSession.sharedInstance().setCategory(AVAudioSession.Category.playback, options: .mixWithOthers)
        try? AVAudioSession.sharedInstance().setActive(true)
        
        motionManager.deviceMotionUpdateInterval = 1/50
        motionManager.startDeviceMotionUpdates(to: .main) { (motion, error) in
            
            if self.lastBump?.timeIntervalSinceNow ?? 0 <= -1 {
                
                /*
                 Processing of fall can occur here.
                 */
                
                self.lastBump = nil
                self.dropDate = nil
                self.endDropDate = nil
                self.bumps = []
                return
            }
            
            if let motion = motion {
                let g = motion.gravity
                let a = motion.userAcceleration
                
                let rawAcc = sqrt(pow((g.x+a.x),2) + pow((g.y+a.y),2) + pow((g.z+a.z),2))
                let isFalling = rawAcc < 0.3
                
                self.view.backgroundColor = isFalling ? .red : UIColor(red: 0, green: 0.8, blue: 0.4, alpha: 1.0)
                
                if isFalling {
                    if self.lastBump?.timeIntervalSinceNow ?? 1 >= 1 {
                        
                        if self.wasPlaying == false {
                            self.lastBump = nil
                            self.dropDate = Date()
                            self.endDropDate = nil
                            
                            self.playScream()
                            self.wasPlaying = true
                            
                            self.speechSynthesis?.stopSpeaking(at: .immediate)
                        }
                        print("Drop is in progress")
                    } else {
                        if self.wasPlaying == false {
                            self.playScream()
                            self.wasPlaying = true
                        }
                    }
                } else if self.wasPlaying {
                    if self.endDropDate == nil {
                        self.endDropDate = Date()
                        print("First bump has occured.")
                    }
                    
                    self.lastBump = Date()
                    
                    self.wasPlaying = false
                    self.playPain(restart: true)
                    
                    //Determine corner bumped
                    let quadrant =
                        g.x*g.y < 0 ? //if a or b is negative
                            (
                                g.x >= 0 ?
                                    2 //+x,-y
                                    :
                                    4 //-x,+y
                            )
                            :
                            (
                                g.x >= 0 ?
                                    1 //+x,+y
                                    :
                                    3 //-x,-y
                    )
                    let corner = Corner(rawValue: quadrant)!
                    
                    print("Bumped \(corner.description)")
                    self.bumps += [corner]
                }
                
            }
        }
        
        
        //Initialize and prepare audio players and voice synthesizers
        speechSynthesis = AVSpeechSynthesizer()
        
        audioPlayerKnock = try? AVAudioPlayer(contentsOf: Bundle.main.url(forResource: "pain", withExtension: "m4a")!)
        audioPlayerKnock?.prepareToPlay()
        
        audioPlayerScream = try? AVAudioPlayer(contentsOf: Bundle.main.url(forResource: "scream", withExtension: "m4a")!)
        audioPlayerScream?.prepareToPlay()
        
        //Workaround on iOS for keeping the app running in the background: indefinitely play an audio file at 0 volume. Requires background audio to be enabled.
        audioBackgrounding = try? AVAudioPlayer.init(contentsOf: Bundle.main.url(forResource: "scream", withExtension: "m4a")!)
        audioBackgrounding?.volume = 0
        audioBackgrounding?.numberOfLoops = -1
        audioBackgrounding?.prepareToPlay()
        audioBackgrounding?.play()
        
    }
    
    func playScream() {
        stopPain()
        audioPlayerScream?.currentTime = 0
        audioPlayerScream?.play()
    }
    
    func stopScream() {
        audioPlayerScream?.stop()
    }
    
    func playPain(restart: Bool = false) {
        if restart {
            audioPlayerKnock?.stop()
        }
        audioPlayerKnock?.play()
        stopScream()
    }
    
    func stopPain() {
        audioPlayerKnock?.stop()
    }
}


