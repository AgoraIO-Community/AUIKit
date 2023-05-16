#
# Be sure to run `pod lib lint AUiKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AUiKit'
  s.version          = '0.1.0'
  s.summary          = 'A short description of AUiKit.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/AgoraIO-Usecase/AUiKit'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'wushengtao' => 'agora@agora.io' }
  s.source           = { :git => 'https://github.com/AgoraIO-Usecase/AUiKit.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '13.0'
  s.xcconfig = {'ENABLE_BITCODE' => 'NO'}

  s.source_files = 'AUiKit/Classes/**/*'
  s.static_framework = true
  
  s.swift_version = '5.0'
  
  s.resource = 'AUiKit/Resource/*.bundle'
  
  # s.resource_bundles = {
  #   'AUiKit' => ['AUiKit/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  # s.frameworks = 'UIKit', 'MapKit'
  s.dependency 'AgoraRtcEngine_iOS'
  s.dependency 'YYModel'
  s.dependency 'SwiftyBeaver', '~>1.9.5'
  s.dependency 'AgoraLyricsScore'
  s.dependency 'Zip'
  s.dependency 'Alamofire'
  s.dependency 'SwiftTheme'
  s.dependency 'Kingfisher', '~>7.6.2'
  s.dependency 'MJRefresh'
  s.dependency 'ScoreEffectUI'
end
