#
# Be sure to run `pod lib lint AUIKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AgoraUIKit_Test'
  s.version          = '0.2.0-beta'
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

  
#s.subspec 'Widgets' do |ss|
#      ss.source_files = [
#       'AgoraUIKit_Test/Classes/Widgets/**/*',
#      'AgoraUIKit_Test/Classes/Core/Utils/Extension/*',
#     'AgoraUIKit_Test/Classes/Core/Utils/Theme/*',
#    'AgoraUIKit_Test/Classes/Core/Utils/Log/*',
#   'AgoraUIKit_Test/Classes/Core/Utils/Localized/*',
#    'AgoraUIKit_Test/Classes/Core/UIConstans/*',
#    'AgoraUIKit_Test/Classes/Core/FoundationExtension/*'
#   ]
#  ss.resource = 'AgoraUIKit_Test/Resource/auiTheme.bundle'
# end
  
#  s.source_files = 'AgoraUIKit_Test/Sources/**/*.swift'
  
    s.source_files = [
        'iOS/AgoraUIKit_Test/Sources/Widgets/**/*',
        'iOS/AgoraUIKit_Test/Sources/Core/Utils/Extension/*.swift',
        'iOS/AgoraUIKit_Test/Sources/Core/Utils/Theme/*.swift',
        'iOS/AgoraUIKit_Test/Sources/Core/Utils/Log/*.swift',
        'iOS/AgoraUIKit_Test/Sources/Core/Utils/Localized/*.swift',
        'iOS/AgoraUIKit_Test/Sources/Core/UIConstans/*.swift',
        'iOS/AgoraUIKit_Test/Sources/Core/FoundationExtension/*.swift'
    ]
    #s.resource = 'AgoraUIKit_Test/Resource/auiTheme.bundle'

  s.static_framework = true
  
  s.swift_version = '5.0'
  
  s.resource = ['iOS/AgoraUIKit_Test/Resource/*.bundle']

  
  # s.resource_bundles = {
  #   'AUIKit' => ['AUIKit/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  s.frameworks = 'UIKit', 'Foundation'
  s.dependency 'AgoraRtcEngine_iOS'
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
