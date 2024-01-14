//
//  AUIJsonParser.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/12/6.
//

import Foundation

func decodeModel<T: Codable>(_ dictionary: [String: Any]) -> T? {
    let decoder = JSONDecoder()
    do {
        let data = try JSONSerialization.data(withJSONObject: dictionary, options: .prettyPrinted)
        let model = try decoder.decode(T.self, from: data)
        return model
    } catch {
        aui_warn("decode model fail: \(error)")
    }
    return nil
}

func decodeModelArray<T: Codable>(_ array: [[String: Any]]) -> [T]? {
    var modelArray: [T] = []
    for dic in array {
        if let model: T = decodeModel(dic) {
            modelArray.append(model)
        }
    }
    if modelArray.count > 0 {
        return modelArray
    }
    return nil
}

func encodeModel(_ model: Codable) -> [String: Any]? {
    let encoder = JSONEncoder()
    encoder.keyEncodingStrategy = .useDefaultKeys
    var dictionary: [String: Any]?
    do {
        let data = try encoder.encode(model)
        dictionary = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
    } catch {
        aui_warn("encode model fail: \(error.localizedDescription)")
        return nil
    }
    
    return dictionary
}


func encodeModelToJsonStr(_ model: Codable) -> String? {
    guard let jsonObj = encodeModel(model) else { return nil }
    guard let data = try? JSONSerialization.data(withJSONObject: jsonObj, options: .prettyPrinted),
          let message = String(data: data, encoding: .utf8) else {
        return nil
    }
    
    return message
}

func decodeToJsonObj(_ jsonStr: String) -> Any? {
    guard let jsonData = jsonStr.data(using: .utf8),
          let jsonObj = try? JSONSerialization.jsonObject(with: jsonData, options: []) else {
        return nil
    }
    
    return jsonObj
}

func encodeToJsonStr(_ jsonObj: Any) -> String? {
    guard let data = try? JSONSerialization.data(withJSONObject: jsonObj, options: .prettyPrinted),
          let value = String(data: data, encoding: .utf8) else {
        return nil
    }
    
    return value
}
