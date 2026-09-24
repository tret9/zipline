package app.cash.zipline.bridge.kotlin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
class ZiplineBridgeCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

  override val pluginOptions = listOf(
    CliOption(
      optionName = C_OUTPUT_DIR_OPTION_NAME,
      valueDescription = "<path>",
      description = "Output directory for generated C bridge files",
      required = false,
    ),
    CliOption(
      optionName = NATIVE_OUTPUT_DIR_OPTION_NAME,
      valueDescription = "<path>",
      description = "Output directory for generated Kotlin/Native bridge source files",
      required = false,
    ),
    CliOption(
      optionName = JS_DISPATCH_OPTION_NAME,
      valueDescription = "true|false",
      description = "Enable JS bridge dispatch injection",
      required = false,
    ),
  )

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration,
  ) {
    when (option.optionName) {
      C_OUTPUT_DIR_OPTION_NAME ->
        configuration.put(COutputDirKey, value)

      NATIVE_OUTPUT_DIR_OPTION_NAME ->
        configuration.put(NativeOutputDirKey, value)

      JS_DISPATCH_OPTION_NAME ->
        configuration.put(JsDispatchKey, value.toBoolean())
    }
  }
}

val COutputDirKey = CompilerConfigurationKey.create<String>("cOutputDir")
val NativeOutputDirKey = CompilerConfigurationKey.create<String>("nativeOutputDir")
val JsDispatchKey = CompilerConfigurationKey.create<Boolean>("jsDispatch")
const val C_OUTPUT_DIR_OPTION_NAME = "cOutputDir"
const val NATIVE_OUTPUT_DIR_OPTION_NAME = "nativeOutputDir"
const val JS_DISPATCH_OPTION_NAME = "jsDispatch"
