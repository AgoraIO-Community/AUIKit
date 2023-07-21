//
//  AUILogger.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation
import SwiftyBeaver


let logger = AUILog.createLog(config: AUILogConfig())
public func aui_info(_ text: String, tag: String = "AUIKit") {
    logger.info(text, context: tag)
}

public func aui_warn(_ text: String, tag: String = "AUIKit") {
    logger.warning(text, context: tag)
}

public func aui_error(_ text: String, tag: String = "AUIKit") {
    logger.error(text, context: tag)
}

@objc class AUILogConfig: NSObject {
    var logFileMaxSize: Int = (2 * 1024 * 1024)
}

@objc class AUILog: NSObject {
    static let formatter = DateFormatter()
    fileprivate static func _dateFormat() ->String {
        formatter.dateFormat = "yyyy-MM-dd"
        
        return formatter.string(from: Date())
        
    }
    
    static func createLog(config: AUILogConfig) -> SwiftyBeaver.Type {
        let log = SwiftyBeaver.self
        
        // add log destinations. at least one is needed!
        let console = ConsoleDestination()
         // log to Xcode Console
        let file = FileDestination()  // log to default swiftybeaver.log file
        let dateString = _dateFormat()
        let logDir = logsDir()
        file.logFileURL = URL(fileURLWithPath: "\(logDir)/auikit_ios_\(dateString)_log.txt")
        
        // use custom format and set console output to short time, log level & message
        console.format = "[AUIKit][$L][$X]$Dyyyy-MM-dd HH:mm:ss.SSS$d $M"
        file.format = console.format
        file.logFileMaxSize = config.logFileMaxSize
        file.logFileAmount = 4
        // or use this for JSON output: console.format = "$J"

        // add the destinations to SwiftyBeaver
        #if DEBUG
        log.addDestination(console)
        #endif
        log.addDestination(file)

        return log
    }
    
    @objc static func cacheDir() ->String {
        let dir = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.cachesDirectory,
                                                      FileManager.SearchPathDomainMask.userDomainMask, true).first
        return dir ?? ""
    }
    
    @objc static func logsDir() ->String {
        let dir = cacheDir()
        let logDir = "\(dir)/aui_logs"
        try? FileManager.default.createDirectory(at: URL(fileURLWithPath: logDir), withIntermediateDirectories: true)
        
        return logDir
    }
}
