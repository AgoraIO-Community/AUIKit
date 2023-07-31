# AgoraRtcEngine
Pod::Spec.new do |spec| 
   spec.name          = "AgoraRtcEngine_iOS" 
   spec.version       = "1.0" 
   spec.summary       = "Agora iOS SDK" 
   spec.description   = "iOS library for agora A/V communication, broadcasting and data channel service." 
   spec.homepage      = "https://docs.agora.io/en/Agora%20Platform/downloads" 
   spec.license       = { "type" => "Copyright", "text" => "Copyright 2022 agora.io. All rights reserved.n"} 
   spec.author        = { "Agora Lab" => "developer@agora.io" } 
   spec.platform      = :ios,9.0 
#   spec.source        = { :git => "" }
   spec.source        = { :http => "https://download.agora.io/sdk/release/Agora_Native_SDK_for_iOS_hyf_63842_FULL_20230428_1607_263060.zip" }
   spec.vendored_frameworks = "*.xcframework"
end 
