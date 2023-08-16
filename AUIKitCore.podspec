#
# Be sure to run `pod lib lint AUIKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AUIKitCore'
  s.version          = '0.4.2'
  s.summary          = 'A short description of AUIKit.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/AgoraIO-Community/AUIKit'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Agora Labs' => 'developer@agora.io' }
  s.source           = { :git => 'https://github.com/AgoraIO-Community/AUIKit.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '13.0'
  s.xcconfig = {'ENABLE_BITCODE' => 'NO'}

  
s.subspec 'Service' do |ss|
      ss.source_files = [
      'iOS/AUIKitCore/Sources/Service/**/*',
      'iOS/AUIKitCore/Sources/Core/Utils/RtmHelper/*',
      'iOS/AUIKitCore/Sources/Core/Utils/Log/*.swift',
      'iOS/AUIKitCore/Sources/Core/Utils/Localized/*.swift',
#      'iOS/AUIKitCore/Sources/Core/UIConstans/*.swift',
      'iOS/AUIKitCore/Sources/Core/Utils/Error/*.swift',
      'iOS/AUIKitCore/Sources/Core/Utils/Context/*.swift',
      'iOS/AUIKitCore/Sources/Core/Utils/Network/**/*',
      'iOS/AUIKitCore/Sources/Core/FoundationExtension/*',
   ]
 end

#  s.source_files = 'iOS/AUIKitCore/Sources/**/*.swift'

s.subspec 'UI' do |ss|
  ss.source_files = [
  'iOS/AUIKitCore/Sources/Widgets/**/*',
  'iOS/AUIKitCore/Sources/Core/Utils/Extension/*.swift',
  'iOS/AUIKitCore/Sources/Core/Utils/Theme/*.swift',
  'iOS/AUIKitCore/Sources/Core/Utils/Log/*.swift',
  'iOS/AUIKitCore/Sources/Core/Utils/Localized/*.swift',
  'iOS/AUIKitCore/Sources/Core/UIConstans/*.swift',
  'iOS/AUIKitCore/Sources/Core/FoundationExtension/*.swift',
  'iOS/AUIKitCore/Sources/Components/**/*',
  'iOS/AUIKitCore/Sources/Service/Extension/API/KTVAPI/*.swift',
  'iOS/AUIKitCore/Sources/Service/Extension/API/FileDownloadCache/*.swift',
  'iOS/AUIKitCore/Sources/Service/Extension/Model/*',
  'iOS/AUIKitCore/Sources/Service/Model/AUIKitModel.swift',
  'iOS/AUIKitCore/Sources/Service/Model/AUIGiftEntity.swift',
  'iOS/AUIKitCore/Sources/Service/Extension/Protocol/AUIUserCellUserDataProtocol.swift',
  ]
  ss.resource = ['iOS/AUIKitCore/Resource/*.bundle']
  end
  
  s.static_framework = true
  
  s.swift_version = '5.0'
  
#  s.resource = ['iOS/AUIKitCore/Resource/*.bundle']

  s.pod_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
  s.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }

  
  # s.resource_bundles = {
  #   'AUIKit' => ['AUIKit/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  s.frameworks = 'UIKit', 'Foundation'
  s.dependency 'AgoraRtcEngine_Special_iOS', '4.1.1.142'
  s.dependency 'AgoraRtm_iOS', '2.1.4'
  s.dependency 'YYModel'
  s.dependency 'SwiftyBeaver', '1.9.5'
  s.dependency 'Zip'
  s.dependency 'Alamofire'
  s.dependency 'SwiftTheme'
  s.dependency 'SDWebImage'
  s.dependency 'MJRefresh'
  s.dependency 'ScoreEffectUI'
  s.dependency 'AgoraLyricsScore'
  s.dependency 'Agora_Chat_iOS'
end
