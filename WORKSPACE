android_sdk_repository(
    name = "androidsdk",
    api_level = 27,
    build_tools_version = "27.0.2",
)

load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_aar")

maven_server(
    name = "maven_google",
    url = "https://maven.google.com/",
)

maven_aar(
    name = "support-appcompat",
    artifact = "com.android.support:appcompat-v7:27.0.1",
)

maven_aar(
    name = "support-design",
    artifact = "com.android.support:design:27.0.1",
    deps = ["@support-appcompat//aar"],
)

maven_aar(
    name = "support-core",
    artifact = "com.android.support:support-v4:27.0.1",
)

maven_aar(
    name = "support-core-utils",
    artifact = "com.android.support:support-core-utils:27.0.1",
)

maven_aar(
    name = "support-core-ui",
    artifact = "com.android.support:support-core-ui:27.0.1",
)

maven_aar(
    name = "support-compat",
    artifact = "com.android.support:support-compat:27.0.1",
)

maven_aar(
    name = "support-fragment",
    artifact = "com.android.support:support-fragment:27.0.1",
)

maven_jar(
    name = "support_annotations",
    artifact = "com.android.support:support-annotations:27.0.1",
    server = "maven_google",
)

maven_jar(
    name = "aarch_lifecycle_common",
    artifact = "android.arch.lifecycle:common:1.0.0",
    server = "maven_google",
)

maven_aar(
    name = "aarch-lifecycle-runtime",
    artifact = "android.arch.lifecycle:runtime:1.0.0",
)

maven_aar(
    name = "aarch-lifecycle-ext",
    artifact = "android.arch.lifecycle:extensions:1.0.0",
)

maven_jar(
    name = "retrofit",
    artifact = "com.squareup.retrofit2:retrofit:2.3.0",
)

maven_jar(
    name = "retrofit_gson",
    artifact = "com.squareup.retrofit2:converter-gson:2.3.0",
)

maven_jar(
    name = "gson",
    artifact = "com.google.code.gson:gson:2.8.1",
)

maven_jar(
    name = "okhttp",
    artifact = "com.squareup.okhttp3:okhttp:3.8.1",
)

maven_jar(
    name = "okhttp_logging",
    artifact = "com.squareup.okhttp3:logging-interceptor:3.7.0",
)

maven_jar(
    name = "picasso",
    artifact = "com.squareup.picasso:picasso:2.5.2",
)
