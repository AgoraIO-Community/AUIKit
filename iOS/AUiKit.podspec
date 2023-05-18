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

  s.subspec 'AUiKit' do |core|
      core.source_files = 'AUiKit/Classes/Core/**/*'
  end
  
  s.subspec 'AUiKit' do |service|
      service.source_files = 'AUiKit/Classes/Service/**/*'
  end

  s.subspec 'AUiKit' do |chat|
      chat.source_files = 'AUiKit/Classes/Components/IM/**/*'
      chat.resource = 'AUiKit/Classes/Components/IM/Resource/VoiceChatRoomResource.bundle'
      chat.dependency 'Agora_Chat_iOS'
      chat.dependency 'AUiKit/Core'
      chat.dependency 'AUiKit/Service'
  end
  
  s.subspec 'AUiKit' do |gift|
      gift.source_files = 'AUiKit/Classes/Components/Gift/**/*'
      gift.resource = 'AUiKit/Classes/Components/IM/Resource/Gift.bundle'
      gift.dependency 'Agora_Chat_iOS'
      gift.dependency 'AUiKit/Core'
      gift.dependency 'AUiKit/Service'
  end
  
  s.subspec 'AUiKit' do |player|
      player.source_files = 'AUiKit/Classes/Components/Player/*'
#      player.resource = 'AUiKit/Classes/Components/Player/Resource/PlayerResource.bundle'
      player.dependency 'ScoreEffectUI'
      player.dependency 'AgoraLyricsScore'
      player.dependency 'AUiKit/Core'
      player.dependency 'AUiKit/Service'
  end
  
  s.source_files = 'AUiKit/Classes/**/*.swift'
  s.static_framework = true
  
  s.swift_version = '5.0'
  
  s.resource = ['AUiKit/Resource/*.bundle', 'AUiKit/Classes/Components/**/Resources/*.bundle']

  
  # s.resource_bundles = {
  #   'AUiKit' => ['AUiKit/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  s.frameworks = 'UIKit', 'Foundation'
  s.dependency 'AgoraRtcEngine_iOS'
  s.dependency 'YYModel'
  s.dependency 'SwiftyBeaver', '~>1.9.5'
  s.dependency 'Zip'
  s.dependency 'Alamofire'
  s.dependency 'SwiftTheme'
  s.dependency 'Kingfisher', '~>7.6.2'
  s.dependency 'MJRefresh'
  s.dependency 'ScoreEffectUI'
  s.dependency 'AgoraLyricsScore'
  s.dependency 'Agora_Chat_iOS'
end
