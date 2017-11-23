android_sdk_repository(
	name = "androidsdk",
	api_level = 27,
	build_tools_version = "27.0.0"
)

load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_aar")
maven_aar(
	name = "appcompat",
	artifact = "com.android.support:appcompat-v7:27.0.1",
)
maven_aar(
	name = "support-design",
	artifact = "com.android.support:design:27.0.1",
)
maven_aar(
	name = "support-v4",
	artifact = "com.android.support:support-v4:27.0.1",
)

see github.com/pubref/rules_kotlin
