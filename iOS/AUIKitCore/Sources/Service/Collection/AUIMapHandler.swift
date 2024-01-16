//
//  AUIMapHandler.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/16.
//

import Foundation

func mergeMap(origMap: [String: Any], newMap: [String: Any]) -> [String: Any] {
    var _origMap = origMap
    newMap.forEach { (k, v) in
        if let dic = v as? [String: Any] {
            let origDic: [String: Any] = _origMap[k] as? [String: Any] ?? [:]
            let newDic = mergeMap(origMap: origDic, newMap: dic)
            _origMap[k] = newDic
        } else {
            //TODO: array ?
            _origMap[k] = v
        }
    }
    return _origMap
}

func calculateMap(origMap: [String: Any],
                  key: [String],
                  value: Int,
                  min: Int,
                  max: Int) -> [String: Any]? {
    var _origMap = origMap
    if key.count > 1 {
        let curKey = key.first ?? ""
        let subKey = Array(key.suffix(from: 1))
        
        guard let subValue = _origMap[curKey] as? [String: Any],
              let newMap = calculateMap(origMap: subValue,
                                        key: subKey,
                                        value: value,
                                        min: min,
                                        max: max) else {
            return nil
        }
        _origMap[curKey] = newMap
        return _origMap
    }
    guard let curKey = key.first, let subValue = _origMap[curKey] as? Int else { return nil }
    let curValue = subValue + value
    guard curValue <= max, curValue >= min else {
        aui_info("calculateMap out of range")
        return nil
    }
    _origMap[curKey] = curValue
    
    return _origMap
}

func getItemIndexes(array: [[String: Any]], filter: [[String: Any]]?) -> [Int]? {
    guard let filter = filter else {
        let indexes = Array(array.indices)
        return indexes.isEmpty ? nil : indexes
    }
    var indexes: [Int] = []
    for (i, value) in array.enumerated() {
        for filterItem in filter {
            var match = true
            for (k, v) in filterItem {
                //only filter String/Bool/Int
                if let valueV = value[k] as? String, let filterV = v as? String {
                    if valueV != filterV {
                        match = false
                        break
                    }
                } else if let valueV = value[k] as? Bool, let filterV = v as? Bool {
                    if valueV != filterV {
                        match = false
                        break
                    }
                } else if let valueV = value[k] as? Int, let filterV = v as? Int {
                    if valueV != filterV {
                        match = false
                        break
                    }
                }
            }
            if match {
                indexes.append(i)
                break
            }
        }
    }
    return indexes.isEmpty ? nil : indexes
}
